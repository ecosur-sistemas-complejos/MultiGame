package mx.ecosur.multigame.model
{
	import mx.ecosur.multigame.entity.Game;
	
	[RemoteClass (alias="mx.ecosur.multigame.model.Game")]
	public class GameModel
	{
		private var _game:Game;
		
		public function GameModel()
		{
			super();
		}
		
		public function get implementation():Game {
			return _game;
		}
		
		public function set implementation(game:Game):void {
			_game = game;
		}
		
		public function toString():String {
			return new String ("GameModel.  Implementation:  " + this.implementation);
		}		
	}
}