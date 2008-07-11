package mx.ecosur.multigame.entity {
	
	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.Player")]
	
	public class Player {
		
		private var _id:int;
		private var _name:String;
		private var _gamecount:int;
		private var _wins:int;
		private var _lastRegistration:int;
		
		public function Player(){
			super();
		}
		
		public function get id():int{
			return _id;
		}
		
		public function set id(id:int):void {
			_id = id;
		}
		
		public function get name():String{
			return _name;
		}
		
		public function set name(name:String):void {
			_name = name;
		}
		
		public function get gamecount():int{
			return _gamecount;
		}
		
		public function set gamecount(gamecount:int):void {
			_gamecount = gamecount;
		}

		public function get wins():int{
			return _wins;
		}
		
		public function set wins(gamecount:int):void {
			_wins = wins;
		}		

		public function get lastRegistration():int{
			return _lastRegistration;
		}
		
		public function set lastRegistration(lastRegistration:int):void {
			_lastRegistration = lastRegistration;
		}
		
		public function toString():String{
			return "id = " + id + ", name = " + name + ", gamecount = " + gamecount + ", lastRegistration = " + lastRegistration + ", wins = " + wins;
		}
	}
}