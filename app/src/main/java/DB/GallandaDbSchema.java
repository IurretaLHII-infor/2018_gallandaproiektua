package DB;

import java.sql.Date;

/**
 * Created by sasiroot on 5/4/18.
 */

public class GallandaDbSchema {
    public static final class DataTable {
        public static final String NAME = "data";
        public static final class Cols {
            public static final String TIMESTAMP = "timestamp";
            public static final String SPEED = "speed";
            public static final String TEMPERATURE = "temperature";
            public static final String BATTERYCHARGE = "batterycharge";
        }
    }
}
