package mx.ecosur.multigame.entity
{
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.Player;

	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.Move")]
	public class Move
	{
		
		private var _id:int;
		private var _player:Player;
		private var _current:Cell;
		private var _destination:Cell;
		private var _status:String;
		
		public static const UNVERIFIED:String = "UNVERIFIED";
		public static const VERIFIED:String = "VERIFIED";
		public static const INVALID:String = "INVALID";
		public static const MOVED:String = "MOVED";
		
		public function Move()
		{
			super();
		}
		
		public function get id():int{
			return _id;
		}
		
		public function set id(id:int):void{
			_id = id;
		}
		
		public function get player():Player{
			return _player;
		}
		
		public function set player(player:Player):void{
			_player = player;
		}
		
		public function get current():Cell{
			return _current;
		}
		
		public function set current(current:Cell):void{
			_current = current;
		}
		
		public function get destination():Cell{
			return _destination;
		}
		
		public function set destination(destination:Cell):void{
			_destination = destination;
		}
		
		public function get status():String{
			return _status;
		}
		
		public function set status(status:String):void{
			_status = status;
		}
		
		
		
	}
}