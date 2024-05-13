package com.app.bsa.service.firestore;

public class DBConstants {


    public static final String SOURCE_CACHE = "Cache";
    public static final String SOURCE_SERVER = "Server";
    public static final String SOURCE_NOTSET = "NotSet";


    public static final String STUDENT_PATH = "Student";
    public static final String DEV_STUDENT_PATH = "dev_Student";

    public static final String BATCHPERMISSIONS_PATH = "BatchPermissions";
    public static final String DEV_BATCHPERMISSIONS_PATH = "dev_BatchPermissions";
    public class STUDENT{
        public static final String FIRST_NAME = "First_name";
        public static final String IS_HALF_PAID = "Is_Half_paid";
        public static final String LAST_NAME = "Last_name";
        public static final String CONTACT = "Contact";
        public static final String BDAY = "Bday";
        public static final String FEE_STATUS = "Fee_status";
        public static final String LEVEL = "Level";
        public static final String BATCH_NAME = "Batch_name";
        public static final String CUSTOM_FEE = "Custom_fee";
        public static final String JOINING_FEE = "Joining_fee";
        public static final String JOINING_FEE_PAID = "Joining_fee_paid";
        public static final String JOINING_FEE_PAID_DT = "Joining_fee_paid_dt";

        public static final String BANK_REF = "Bank_ref";
        public static final String SKILLS = "skills";
        public static final String LAST_ATTENDED = "Last_attended";
        public static final String MONTHLY_ATTENDANCE_COUNT = "Monthly_attendance_count";
        public static final String FEE_RCVD_DT = "Fee_Rcvd_dt";
        public static final String JOINING_DT = "Joining_dt";
        public static final String ATTENDENCE_SUMMARY = "Attendance_Summary";

    }

    public static final String ADMIN_PATH = "Admin";
    public static final String DEV_ADMIN_PATH = "dev_Admin";
    public class ADMIN{
        public static final String ID = "id";
        public static final String ADMIN_WITH_NO_FILTER = "_nofilter";
    }

    public static final String FEES_PATH = "Fees";
    public static final String DEV_FEES_PATH = "dev_Fees";
    public class FEES{
        public static final String FEE_LEVEL = "Fee_level";
        public static final String FEE_AMOUNT = "Fee_amount";

        //Not a field = default fee value
        public static final String DEFAULT = "Default";

    }

    public static final String COACH_PATH = "Coach";
    public static final String DEV_COACH_PATH = "dev_Coach";
    public class COACH{
        public static final String COUNT = "count";
        public static final String DATE = "date";

        //Not a field = default fee value
        public static final String EMAIL = "email";

    }

    public class BATCH{
        public static final String batch_name = "batch_name";
        public static final String permission = "permission";

    }
}
