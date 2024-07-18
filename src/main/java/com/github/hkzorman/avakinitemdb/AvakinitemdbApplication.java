package com.github.hkzorman.avakinitemdb;

import com.github.hkzorman.avakinitemdb.models.db.AvakinItem;
import com.github.hkzorman.avakinitemdb.repositories.AvakinItemRepository;
import com.github.hkzorman.avakinitemdb.services.AvakinItemSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.LinkedList;

@SpringBootApplication
public class AvakinitemdbApplication {

	private Logger logger = LoggerFactory.getLogger(AvakinitemdbApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AvakinitemdbApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(AvakinItemSyncService service, AvakinItemRepository repository) {
		return args -> {

			var items = new LinkedList<AvakinItem>();
			if (repository.count() == 0) {
				items.addAll(service.retrieveAll());
			}
			else {
				items.addAll(service.synchronize());
			}

			logger.info("Completed synchronizing local DB with Avakin DB (" + items.size() + " items)");
		};
	}
}
