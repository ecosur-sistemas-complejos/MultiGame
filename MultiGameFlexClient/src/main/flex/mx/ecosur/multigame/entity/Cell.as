package mx.ecosur.multigame.entity {
	
	import mx.core.UIComponent;
	import mx.ecosur.multigame.enum.Color;
	
	/**
	 * Represents a server side cell object
	 */
	[RemoteClass (alias="mx.ecosur.multigame.Cell")]
	public class Cell extends UIComponent{
		
		private var _row:int;
		private var _column:int;
		private var _color:String;
		private var _colorCode:uint;
		private var _player:Player;
		
		public function Cell(){
			super();
		}
		
		/* Getters and setters */
		
		public function get row():int{
			return _row;
		}
		
		public function set row(row:int):void {
			_row = row;
		}
		
		public function get column():int{
			return _column;
		}
		
		public function set column(column:int):void {
			_column = column;
		}
		
		public function get color():String{
			return _color;
		}
		
		public function set color(color:String):void{
			_colorCode = Color.getColorCode(color);
			_color = color;
		}
		
		public function get colorCode():uint{
			return _colorCode;
		}
		
		public function get player():Player{
			return _player;
		}
		
		public function set player(player:Player):void{
			_player = player;
		}
		
		/**
		 * Returns a clone of the actual cell. The clone
		 * is not recursive.
		 * 
		 * @return the cloned cell
		 */
		public function clone():Cell{
			var clone:Cell = new Cell();
			clone.color = _color;
			clone.column = _column;
			clone.row = _row;
			clone.player = _player;
			return clone; 
		}
		
		/* Overrrides */
		
		override public function toString():String{
			return "id = " + id + ", color = " + color + ", colorCode = " + ",  column = " + column + ", row = " + row + " player = " + player; 
		}
	}
}