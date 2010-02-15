package mx.ecosur.multigame.manantiales.entity
{
    import flash.events.Event;

    import mx.controls.Alert;
    import mx.controls.Menu;
    import mx.controls.PopUpButton;
    import mx.ecosur.multigame.entity.Cell;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.manantiales.Ficha")]
	public class Ficha extends Cell
	{
		protected var _type:String;

        private var popup:PopUpButton;
		
		public function Ficha()
		{
		  super();
		}
		
		public function get type():String {
	       return this._type;		
		}
		
		public function set type (type:String):void {
			this._type = type;
		}

        public function suggestionHandler(event:Event) :void {
            Alert.show("Suggestion handled!");
        }

        public function activateSuggestions ():void {
            Alert.show("Suggestions activated.");
            var popUpMenu:Menu = new Menu();
            popUpMenu.dataProvider = [
            {label: "Move"}];
            popUpMenu.addEventListener("itemClick", suggestionHandler);

            popup = new PopUpButton();
            popup.label= "Suggest";
            popup.popUp = popUpMenu;
            addChild(popup);
        }

        public function deactivateSuggestions ():void {
            removeChild (popup);
            popup = null;
        }        
		
        /**
         * Returns a clone of the actual ficha. The clone
         * is not recursive.
         * 
         * @return the cloned cell
         */
        override public function clone():Cell{
            var clone:Ficha = new Ficha();
            clone.color = _color;
            clone.column = _column;
            clone.row = _row;
            clone.type = _type;
            return clone; 
        }		
	}
}