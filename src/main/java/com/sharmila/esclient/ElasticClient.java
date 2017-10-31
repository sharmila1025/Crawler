package com.sharmila.esclient;

import java.util.concurrent.TimeUnit;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public enum ElasticClient {
	CLIENT;

	private final Client client;
	private final TransportClient transportClient;

	private ElasticClient() {
		// logger.info("Creating new elasticsearch client object...");
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch")
				.put("client.transport.sniff", false).put("client.transport.ping_timeout", 30, TimeUnit.SECONDS)
				.build();

		transportClient = new TransportClient(settings);
		this.client = transportClient.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	}

	public Client getInstance() {
		return this.client;
	}

	public void destory() {
		this.client.close();
	}
}
