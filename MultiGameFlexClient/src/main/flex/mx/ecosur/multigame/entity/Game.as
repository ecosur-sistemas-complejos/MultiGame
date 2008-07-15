package mx.ecosur.multigame.entity {
	
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.GamePlayer;

	/**
	 * Represents a server side Game object. 
	 * Only the id is saved on the client side.
	 */
	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.Game")]
	public class Game {
		
		private var _id:int;
		
		public function Game() {
			super();
		}
		
		public function get id():int{
			return _id;
		}
		
		public function set id(id:int):void{
			_id = id;
		}	
		
		public function toString():String{
			return "id = " + id;
		}
		
	}
}