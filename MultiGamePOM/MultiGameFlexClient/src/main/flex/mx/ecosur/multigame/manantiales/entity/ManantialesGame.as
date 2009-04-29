package mx.ecosur.multigame.manantiales.entity
{
	import mx.ecosur.multigame.entity.Game;

    [RemoteClass (alias=
        "mx.ecosur.multigame.ejb.entity.manantiales.ManantialesGame")]                       
	public class ManantialesGame extends Game
	{
//		private var _mode:String;
		
		public function ManantialesGame()
		{
			super();
		}
		
//		public function get mode():String {
//			return _mode;
//		}
//		
//		public function set mode(mode:String) {
//			_mode = mode;
//		}		
	}
}