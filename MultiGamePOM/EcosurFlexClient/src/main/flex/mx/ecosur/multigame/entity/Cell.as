/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.entity {
	
	import mx.core.UIComponent;
	import mx.ecosur.multigame.enum.Color;
	
	/**
	 * Represents a server side cell object
	 */
	[RemoteClass (alias="mx.ecosur.multigame.impl.model.GridCell")]
	public class Cell extends UIComponent{
		
		protected var _row:int;
		protected var _column:int;
		protected var _color:String;
		protected var _colorCode:uint;
		protected var _player:Registrant;
		
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
		
		public function get player():Registrant{
			return _player;
		}
		
		public function set player(player:Registrant):void{
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