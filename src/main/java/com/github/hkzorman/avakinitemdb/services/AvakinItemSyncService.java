package com.github.hkzorman.avakinitemdb.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hkzorman.avakinitemdb.models.api.AvakinApiItemPagedResponse;
import com.github.hkzorman.avakinitemdb.models.api.AvakinApiItemSummary;
import com.github.hkzorman.avakinitemdb.models.db.AvakinItem;
import com.github.hkzorman.avakinitemdb.repositories.AvakinItemRepository;
import com.github.hkzorman.avakinitemdb.utils.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class AvakinItemSyncService {

    // Use https://avkndb.vercel.app/api/items to get all items. Pages will happen
    // Use https://avkndb.vercel.app/api/items/categorys to get the categories. We only want: female0, male0, animation

    private static final String apiBaseUrl = "https://avkndb.vercel.app/api/items";
    private static final String[] categories = {"female01", "male01", "animation"};

    private Logger logger = LoggerFactory.getLogger(AvakinItemSyncService.class);
    private AvakinItemRepository itemRepository;
    private HttpClient httpClient;

    public AvakinItemSyncService(AvakinItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<AvakinItem> retrieveAll() throws Exception {
        var summaries = new LinkedList<AvakinApiItemSummary>();
        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        var path = Paths.get(System.getProperty("user.dir"), "summaries.json");

        // First get all summaries
        if (Files.exists(path)) {
            var mapper = new ObjectMapper();
            summaries = new LinkedList<>(Arrays.asList(mapper.readValue(Files.readString(path), AvakinApiItemSummary[].class)));
            logger.info("Logged summaries from file. Found: " + summaries.size());
        }
        else {
            // Retrieve all summaries and save to file
            var itemCountResults = performParallelRequest(new ItemSummaryCallableFactory(), this.httpClient, categories);

            var summaryTasks = new ArrayList<ItemSummaryCallable>(categories.length);

            var i = 0;
            for (var category : categories) {
                logger.info("Querying for category: " + category);
                var pages = itemCountResults.get(i++).getPagesCount();
                for (int page = 0; page <= pages; page++) {
                    summaryTasks.add(new ItemSummaryCallable(this.httpClient, category, page));
                }
            }

            List<Future<AvakinApiItemPagedResponse>> results = executor.invokeAll(summaryTasks);
            executor.shutdown();

            for (var future : results) {
                summaries.addAll(Arrays.asList(future.get().getItems()));
            }

            logger.info("Finished retrieving item summaries for all categories: " + summaries.size());

            var mapper = new ObjectMapper();
            Files.write(path, mapper.writeValueAsBytes(summaries));
        }

        var itemIds = summaries.stream().map(x -> x.getItemId()).collect(Collectors.toSet());
        return retrieveItemDetails(itemIds, 4);
    }

    public List<AvakinItem> synchronize() throws Exception {
        var result = new LinkedList<AvakinItem>();

        // First, retrieve the first page of each category which state the total items
        var itemCountResults = performParallelRequest(new ItemSummaryCallableFactory(), this.httpClient, categories);
        logger.info("Results: " + itemCountResults.get(0).getItemsCount() + ", " + itemCountResults.get(1).getItemsCount() + ", " + itemCountResults.get(2).getItemsCount());

        // Retrieve the stored item counts
        var allExistingItems = new ArrayList<List<AvakinItem>>(categories.length);
        for (var category : categories) { allExistingItems.add(itemRepository.findByType(category).get()); }

        for (int i = 0; i < categories.length; i++) {
            var initialPage = itemCountResults.get(i);
            var existingItemsInCategory = allExistingItems.get(i);
            var totalCount = initialPage.getItemsCount();
            var existingCount = existingItemsInCategory.size();
            if (totalCount > existingCount) {
                logger.info("New items found for category: " + categories[i] + " (" + totalCount + " vs " + existingCount + ")");

                var newItems = retrieveNewItems(initialPage, existingItemsInCategory);
                result.addAll(newItems);
            }
            else {
                logger.info("No new items found for category '" + categories[i] + "'. Up to date");
            }
        }

        return result;
    }

    private List<AvakinItem> retrieveNewItems(AvakinApiItemPagedResponse initialPage, List<AvakinItem> existingItems) throws Exception {
        var category = existingItems.get(0).getType();
        var result = new LinkedList<AvakinItem>();
        var newItemCount = initialPage.getItemsCount() - existingItems.size();

        // Calculate number of pages to retrieve
        if (newItemCount < 24) {
            logger.info("Retrieving " + newItemCount + " new items");
            // Items can be taken from initialPage
            var potentialNewItems = Arrays
                    .stream(initialPage.getItems())
                    .limit(newItemCount)
                    .map(x -> x.getItemId())
                    .collect(Collectors.toSet());

            var existingItemIds = existingItems
                    .stream()
                    .map(x -> x.getItemId())
                    .collect(Collectors.toSet());

            potentialNewItems.removeAll(existingItemIds);

            logger.info("Retrieved " + potentialNewItems.size() + " potential new items from API");

            if (!potentialNewItems.isEmpty()) {
                result.addAll(retrieveItemDetails(potentialNewItems, 4));
            }
        }
        else {
            var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            int pages = newItemCount / 24;
            int limit = newItemCount % 24;
            if (limit > 0) pages++; // We need an additional call
            int itemLimit = limit > 0 ? 24 * (pages - 1) + limit : 24 * pages;

            logger.info("Will retrieve " + itemLimit + " in " + pages + " batches");

            var summaryTasks = new ArrayList<ItemSummaryCallable>(pages);
            var items = new HashSet<String>();
            for (var page = 0; page < pages; page++) {
                summaryTasks.add(new ItemSummaryCallable(this.httpClient, category, page));
            }

            List<Future<AvakinApiItemPagedResponse>> results = executor.invokeAll(summaryTasks);
            executor.shutdown();

            logger.info("Finished API call");

            for (var future : results) {
                var itemIds = Arrays
                        .stream(future.get().getItems())
                        .limit(itemLimit)
                        .map(AvakinApiItemSummary::getItemId)
                        .toList();
                items.addAll(itemIds);
            }

            logger.info("Retrieved " + items.size() + " item IDs: [" + items.stream().collect(Collectors.joining(",")) + "]");

            result.addAll(retrieveItemDetails(items, 4));
        }

        return result;
    }

    private List<AvakinItem> retrieveItemDetails(Set<String> itemIds, int batchSize) throws Exception {
        // Query all item details
        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        var items = new ArrayList<AvakinItem>(itemIds.size());

        logger.info("Retrieving " + itemIds.size() + " item details in batches of " + batchSize);

        for (int i = 0; i < (itemIds.size() / batchSize); i++) {
            var detailTasks = new ArrayList<ItemDetailCallable>(batchSize);
            var batch = itemIds.stream().skip(batchSize * i).limit(batchSize).collect(Collectors.toList());

            for (var itemId : batch) {
                detailTasks.add(new ItemDetailCallable(this.httpClient, itemId));
            }

            List<Future<AvakinItem>> futureItems = executor.invokeAll(detailTasks);
            var currentItems = futureItems.stream().map(x -> {
                try {
                    return x.get();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }).collect(Collectors.toList());
            currentItems.forEach(x -> itemRepository.save(x));

            logger.info("Saved " + currentItems.size() + " items to the database");
            for (var future : futureItems) {
                items.add(future.get());
            }

            Thread.sleep(1200);
        }

        executor.shutdown();

        return items;
    }

    private <T1, T2> List<T1> performParallelRequest(CallableFactory<T1> factory, HttpClient client, T2[] args) throws Exception {
        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // First, retrieve the first page for each category
        var tasks = new ArrayList<Callable<T1>>(args.length);
        for (var arg : args) {
            tasks.add(factory.create(client, arg));
        }

        var itemCountFutures = executor.invokeAll(tasks);
        var result = itemCountFutures
                .stream()
                .map(FutureUtil::getFutureSafe)
                .toList();

        executor.shutdown();

        return result;
    }

    private URI getUri(Optional<Integer> page, Optional<String> type, Optional<String> subType) {
        var uri = UriComponentsBuilder.fromHttpUrl(apiBaseUrl);

        page.ifPresent(integer -> uri.queryParam("page", integer));
        type.ifPresent(string -> uri.queryParam("category", string));
        subType.ifPresent(string -> uri.queryParam("subCategory", string));

        return uri.build().toUri();
    }

    private class ItemSummaryCallable implements Callable<AvakinApiItemPagedResponse> {

        private HttpClient client;
        private final String category;
        private final int page;

        public ItemSummaryCallable(HttpClient client, String category) {
            this(client, category, 0);
        }
        public ItemSummaryCallable(HttpClient client, String category, int page) {
            this.client = client;
            this.category = category;
            this.page = page;
        }

        @Override
        public AvakinApiItemPagedResponse call() throws Exception {
            var request = HttpRequest
                    .newBuilder()
                    .GET()
                    .uri(getUri(Optional.of(page), Optional.of(category), Optional.empty()))
                    .build();

            var httpResponse = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            var mapper = new ObjectMapper();
            return mapper.readValue(httpResponse.body(), AvakinApiItemPagedResponse.class);
        }
    }

    private class ItemDetailCallable implements Callable<AvakinItem> {

        private HttpClient client;
        private String itemId;

        public ItemDetailCallable(HttpClient client, String itemId) {
            this.client = client;
            this.itemId = itemId;
        }

        @Override
        public AvakinItem call() throws Exception {
            logger.info("Retrieving details for item with ID: " + this.itemId);
            var uri = UriComponentsBuilder.fromHttpUrl(apiBaseUrl).path("/" + this.itemId).build().toUri();
            var request = HttpRequest.newBuilder(uri).GET().build();

            var httpResponse = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Received response: " + httpResponse.body());
            var mapper = new ObjectMapper();
            return mapper.readValue(httpResponse.body(), AvakinItem.class);
        }
    }

    private interface CallableFactory<T> {
        Callable<T> create(Object... args);
    }

    private class ItemSummaryCallableFactory implements CallableFactory<AvakinApiItemPagedResponse> {

        @Override
        public Callable<AvakinApiItemPagedResponse> create(Object... args) {
            return new ItemSummaryCallable((HttpClient)args[0], (String)args[1]);
        }
    }

    private class ItemDetailCallableFactory implements CallableFactory<AvakinItem> {

        @Override
        public Callable<AvakinItem> create(Object... args) {
            return new ItemDetailCallable((HttpClient)args[0], (String)args[1]);
        }
    }
}
