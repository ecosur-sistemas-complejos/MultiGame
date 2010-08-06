package mx.ecosur.multigame.oculto.entity
{
	import mx.ecosur.multigame.entity.Move;
	import mx.ecosur.multigame.gente.entity.StrategyPlayer;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.oculto.OcultoMove")]
	public class PasaleMove extends Move
	{
		private var _badYear:Boolean;
		
		private var _mode:String;
		
		public function PasaleMove()
		{
			super();
			_badYear = false;
		}
		
		public function get badYear():Boolean {
			return _badYear;
		}
		
		public function set badYear (year:Boolean):void {
			_badYear = year;
		}
		
		public function get mode ():String {
			return _mode;
		}
		
		public function set mode (mode:String):void {
			_mode = mode;
		}
	}
}