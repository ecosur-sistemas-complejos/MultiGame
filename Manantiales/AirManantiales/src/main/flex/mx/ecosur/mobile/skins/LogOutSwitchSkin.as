/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 3/1/12
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.mobile.skins {

    import spark.skins.mobile.ToggleSwitchSkin;

    public class LogOutSwitchSkin extends ToggleSwitchSkin {

        public function LogOutSwitchSkin() {
            super();
            selectedLabel = "Login";
            unselectedLabel = "Logout";
        }
    }
}
