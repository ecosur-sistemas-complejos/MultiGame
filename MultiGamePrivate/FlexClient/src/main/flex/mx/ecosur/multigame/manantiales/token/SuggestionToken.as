//copyright

package mx.ecosur.multigame.manantiales.token
{
    import mx.ecosur.multigame.manantiales.entity.*;
    import flash.events.Event;

    import mx.controls.Alert;
    import mx.controls.Menu;
    import mx.controls.PopUpButton;

    public class SuggestionToken extends Ficha {

        var button:PopUpButton;

        public function suggestionHandler(event:Event) :void {
            Alert.show("Suggestion handled!");
        }

        public function activateSuggestions (menu:Menu):void {
            button = new PopUpButton();
            Alert.show ("Adding suggestion menu to ficha");
            menu.addEventListener("itemClick", suggestionHandler);
            button.popUp = menu;
            addChild(button);
        }

        public function deactivateSuggestions ():void {
            removeChild (button);
            button = null;
        }
    }
}
