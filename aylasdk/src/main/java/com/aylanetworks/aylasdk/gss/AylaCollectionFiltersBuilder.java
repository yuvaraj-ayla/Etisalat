package com.aylanetworks.aylasdk.gss;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
import java.util.HashMap;
import java.util.Map;

/**
 * Collection filters builder used to build query parameters
 * when fetching collections from the cloud.
 */
public class AylaCollectionFiltersBuilder {

    private String type;
    private Boolean paginated = true;
    private int per_page = 10;
    private int page = 1;
    private String sort_by = "created_at";
    private String order_by = "asc";

    public AylaCollectionFiltersBuilder withCollectionType(String type) {
        this.type = type;
        return this;
    }

    public AylaCollectionFiltersBuilder withPaginated(Boolean paginated) {
        this.paginated = paginated;
        return this;
    }

    public AylaCollectionFiltersBuilder withPage(int page) {
        this.page = page;
        return this;
    }

    public AylaCollectionFiltersBuilder withPerPage(int per_page) {
        this.per_page = per_page;
        return this;
    }

    public AylaCollectionFiltersBuilder withSortBy(String sort_by) {
        this.sort_by = sort_by;
        return this;
    }

    public AylaCollectionFiltersBuilder withOrderBy(String order_by) {
        this.order_by = order_by;
        return this;
    }

    public Map<String, String> build() {
        Map<String, String> map = new HashMap<>();

        map.put("per_page", String.valueOf(per_page > 0 ? per_page : 10));
        map.put("page", String.valueOf(page > 0 ? page : 1));
        map.put("paginated", String.valueOf(paginated));

        if (type != null) {
            map.put("type", type);
        }

        if (sort_by != null) {
            map.put("sort_by", sort_by);
        }

        if (order_by != null) {
            map.put("order_by", order_by);
        }
        return map;
    }
}
