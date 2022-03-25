package com.aylanetworks.aylasdk.icc.userconsent;

import androidx.annotation.IntRange;
import androidx.annotation.StringDef;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//    {
//        "total": 2,
//            "devices": [
//        {
//            "dsn": "TESTDSN_508963_000",
//                "device_jobs": [
//            {
//                "id": "pXNJANXv",
//                    "name": "post_job_test",
//                    "job_type": "OTA",
//                    "delivery_option": "USER_CONSENT",
//                    "started_at": "2019-10-16T16:33:52+0000",
//                    "stopped_at": null,
//                    "description": "TEST Job",
//                    "job_metadata": [],
//                "device_status": "CONSENT"
//            }
//            ]
//        },
//        {
//            "dsn": "TESTDSN_508963_001",
//                "device_jobs": [
//            {
//                "id": "pXNJANXv",
//                    "name": "post_job_test",
//                    "job_type": "OTA",
//                    "delivery_option": "USER_CONSENT",
//                    "started_at": "2019-10-16T16:33:52+0000",
//                    "stopped_at": null,
//                    "description": "TEST Job",
//                    "job_metadata": [],
//                "device_status": "CONSENT"
//            }
//            ]
//        }
//    ],
//        "previous_page": null,
//            "next_page": null,
//            "current_page_number": 1,
//            "start_count_on_page": 1,
//            "end_count_on_page": 2
//    }
public class AylaUserConsentJob {

    @Expose
    public int total;
    @Expose
    public Device[] devices;
    @Expose
    public int previous_page;
    @Expose
    public int next_page;
    @Expose
    public int current_page_number;
    @Expose
    public int start_count_on_page;
    @Expose
    public int end_count_on_page;

    public static class Device {
        @Expose
        public String dsn;
        @Expose
        public DeviceJob[] device_jobs;
    }

    public static class DeviceJob implements Serializable {
        @Expose
        public String id;
        @Expose
        public String name;
        @Expose
        public String job_type;
        @Expose
        public String delivery_option;
        @Expose
        public String started_at;
        @Expose
        public String stopped_at;
        @Expose
        public String description;
        @Expose
        public String device_status;
    }

    public static class JobType {
        @StringDef({OTA, FILE_TRANSFER, SET_PROPERTIES})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedType {
        }

        public static final String OTA = "OTA";
        public static final String FILE_TRANSFER = "FILE_TRANSFER";
        public static final String SET_PROPERTIES = "SET_PROPERTIES";
    }

    public static class DeliveryOption {
        @StringDef({USER_CONSENT, SYSTEM_PUSH})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedType {
        }

        /**
         * Jobs that are to be delivered to device with user's consent.
         */
        public static final String USER_CONSENT = "USER_CONSENT";

        /**
         * Jobs that are to be delivered to device without user's consent.
         */
        public static final String SYSTEM_PUSH = "SYSTEM_PUSH";

    }

    public static class DeviceStatus {
        @StringDef({PENDING, CONSENT, PROCESSING, SCHEDULED,QUEUED, WAITING,
                SUCCEED, FAILED, PARTIALY_FAILED, CANCELLED /**, SENT,
                RETRY, DOWNLOADING, DOWNLOADED*/})
        @Retention(RetentionPolicy.SOURCE)

        public @interface AllowedType {
        }

        /**
         * The device is being queued on backend, waiting to be processed.
         */
        public static final String PENDING = "PENDING";

        /**
         * The device is waiting for user to decide when it can go on.
         * <em>Only in this status the device can accept a “PUT” request in the cloud.</em>
         */
        public static final String CONSENT = "CONSENT";

        /**
         * The device is being scheduled to take the action.
         */
        public static final String SCHEDULED = "SCHEDULED";

        /**
         * The device is being processed.
         */
        public static final String PROCESSING = "PROCESSING";

        /**
         * The device has done with the assigned job action with success.
         */
        public static final String SUCCEED = "SUCCEED";

        /**
         * The action assigned to the device has been failed.
         */
        public static final String FAILED = "FAILED";

        /**
         * There are some of assigned action that are failed, but some of them succeeded.
         */
        public static final String PARTIALY_FAILED = "PARTIALY_FAILED";

        /**
         * The action has been cancelled by user or OEM user/admin.
         */
        public static final String CANCELLED = "CANCELLED";

        /**
         * The action has been queued to be performed later
         */
        public static final String QUEUED = "QUEUED";

        /**
         * Waiting to start
         */
        public static final String WAITING = "WAITING";

        /**
         * The following status are not yet implemented, byt are slated to be implemented
         *
        public static final String RETRY = "RETRY";


        public static final String DOWNLOADING = "DOWNLOADING";


        public static final String DOWNLOADED = "DOWNLOADED";

        public static final String SENT = "SENT";
         
         */

    }


    public static class UpdateUserConsentJobsResult {
//    {
//        "succeeded": [],
//        "failed": [
//        {
//            "dsn": "TESTDSN_508963_000",
//            "device_jobs": [
//            {
//                "id": "pXNJANXv",
//                "name": "post_job_test_neil",
//                "job_type": "OTA",
//                "delivery_option": "USER_CONSENT",
//                "started_at": "2019-10-16T16:33:52+0000",
//                "stopped_at": "2019-10-21T06:34:35+0000",
//                "description": "TEST Job",
//                "job_metadata": [],
//                "device_status": "PROCESSING",
//                "schedule_timestamp": 0
//            }
//           ]
//        }
//       ]
//    }

        @Expose
        public Device[] succeeded;
        @Expose
        public Device[] failed;
    }

    public static class FiltersBuilder {
        private String jobType;
        private Date from;
        private Date to;
        private String[] status;
        private String oem_model;
        private String[] dsns;
        private int page = 1;
        private int per_page = 50;
        private String order_by;
        private String order = "ASC";

        public FiltersBuilder withJobType(@JobType.AllowedType String jobType) {
            this.jobType = jobType;
            return this;
        }

        public FiltersBuilder withFrom(Date fromDate) {
            this.from = fromDate;
            return this;
        }

        public FiltersBuilder withTo(Date toDate) {
            this.to = toDate;
            return this;
        }

        public FiltersBuilder withStatus(@DeviceStatus.AllowedType String[] status) {
            this.status = status;
            return this;
        }

        public FiltersBuilder withOemModel(String oemModel) {
            this.oem_model = oemModel;
            return this;
        }

        public FiltersBuilder withDSNs(String[] dsns) {
            this.dsns = dsns;
            return this;
        }

        public FiltersBuilder withStartPage(@IntRange(from = 1) int startPage) {
            this.page = startPage;
            return this;
        }

        public FiltersBuilder withPerPage(@IntRange(from = 5, to = 200) int perPage) {
            this.per_page = perPage;
            return this;
        }

        public FiltersBuilder withOrder(String order) {
            this.order = order;
            return this;
        }

        public FiltersBuilder withOrderBy(String orderBy) {
            this.order_by = orderBy;
            return this;
        }

        public Map map() {
            Map<String, String> map = new HashMap<>();
            map.put("page", String.valueOf(page > 0 ? page : 1));
            map.put("per_page", String.valueOf(per_page));

            if (jobType != null) {
                map.put("job_type", jobType);
            }

            if (from != null) {
                map.put("from", from.toString());
            }

            if (to != null) {
                map.put("to", to.toString());
            }

            if (status != null) {
                StringBuilder statusBuilder = new StringBuilder();
                for (String jobStatus : status) {
                    statusBuilder.append(jobStatus).append(",");
                }
                String statusString = statusBuilder.toString();
                int len = statusString.length() - 1;
                map.put("status", statusBuilder.toString().substring(0, len));
            }

            if (oem_model != null) {
                map.put("oem_model", oem_model);
            }

            if (dsns != null) {
                StringBuilder dsnsBuilder = new StringBuilder();
                for (String dsn : dsns) {
                    dsnsBuilder.append(dsn).append(",");
                }
                String dsnsString = dsnsBuilder.toString();
                int len = dsnsString.length() - 1; // ignore trailing comma(',')
                map.put("dsns", dsnsBuilder.toString().substring(0, len));
            }

            if (order_by != null) {
                map.put("order_by", order_by);
            }

            if (order != null) {
                map.put("order", order);
            }

            return map;
        }
    }

}
