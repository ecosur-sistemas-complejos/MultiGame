package mx.ecosur.multigame.entity {
	
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.GamePlayer;

	/**
	 * Represents a server side Game object. 
	 * Not all server side properties are represented on
	 * the client for speed of parsing
	 */
	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.Game")]
	public class Game {
		
		private var _id:int;
		
		private var _rows:int;
		
		private var _columns:int;
		
		private var _state:String;
		
		public function Game() {
			super();
		}
		
		public function get id():int{
			return _id;
		}
		
		public function set id(id:int):void{
			_id = id;
		}	
		
		public function get rows():int{
			return _rows;
		}
		
		public function set rows(rows:int):void{
			_rows = rows;
		}
		
		public function get columns():int{
			return _columns;
		}
		
		public function set columns(columns:int):void{
			_columns = columns;
		}
		
		public function get state():String{
			return _state;
		}
		
		public function set state(state:String):void{
			_state = state;
		}
		
		public function toString():String{
			return "id = " + _id + ", rows = " + _rows + ", columns = " +  _columns;
		}
		
	}
}