/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 3/1/12
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.mobile.skins {

    import spark.skins.mobile.ToggleSwitchSkin;

    [ResourceBundle("ManantialesAir")]
    public class LogInSwitchSkin extends ToggleSwitchSkin {

        public function LogInSwitchSkin() {
            super();
            selectedLabel = resourceManager.getString('ManantialesAir','login.switch.Login');
            unselectedLabel = resourceManager.getString('ManantialesAir','login.switch.Logout');
        }
    }
}
