/**
 * Created by pb5n0179 on 8/19/2015.
 */
import models.*;
import play.*;
import play.jobs.*;
import play.test.*;

@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
        // Check if the database is empty
        if(User.count() == 0) {
            Fixtures.loadModels("initial-data.yml");
        }
    }
}
