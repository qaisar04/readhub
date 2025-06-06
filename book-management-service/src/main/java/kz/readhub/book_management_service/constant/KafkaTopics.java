package kz.readhub.book_management_service.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaTopics {

    public static final String BOOK_CDC_EVENTS = "content.catalog.book.cdc.v1";

    public static final String BOOK_ANALYTICS_EVENTS = "analytics.content.book.metrics.v1";
}
