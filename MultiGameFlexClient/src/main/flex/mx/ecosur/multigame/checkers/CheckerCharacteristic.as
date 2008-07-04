package mx.ecosur.multigame.checkers
{
	import mx.ecosur.multigame.entity.Characteristic;

	[RemoteClass (alias="mx.ecosur.multigame.checkers.CheckerCharacteristic")]
	public class CheckerCharacteristic implements Characteristic {
	
		private var _kinged:Boolean;

		public function CheckerCharacteristic () {
			super();
			_kinged = false;
		}
	
		public function get kinged():Boolean {
			return _kinged;
		}

		public function set kinged(kinged:Boolean):void {
			_kinged = kinged;
		}
	}
}