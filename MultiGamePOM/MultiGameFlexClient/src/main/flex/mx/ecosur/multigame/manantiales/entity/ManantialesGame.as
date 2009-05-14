package mx.ecosur.multigame.manantiales.entity
{
	import mx.ecosur.multigame.entity.Game;

    [RemoteClass (alias=
        "mx.ecosur.multigame.ejb.entity.manantiales.ManantialesGame")]                       
	public class ManantialesGame extends Game
	{
		private var _mode:String;
		
		public function get mode():String {
			return _mode;
		}
		
		public function set mode(mode:String):void {
			_mode = mode;
		}		
	}
}