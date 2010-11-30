package mx.ecosur.multigame.model
{
    import mx.ecosur.multigame.enum.GameState
    import mx.ecosur.multigame.model.interfaces.GameImplementation;
	
	[RemoteClass (alias="mx.ecosur.multigame.model.Game")]
	public class GameModel
	{
		private var _game:GameImplementation;

        private var _state:GameState;

		public function GameModel()
		{
			super();
        }

        public function get id():int {
            return _game.id;
        }
		
		public function get implementation():GameImplementation {
			return _game;
		}
		
		public function set implementation(game:GameImplementation):void {
			_game = game;
		}

        public function get state():GameState {
            return _state;
        }

        public function set state (state:GameState):void {
            _state = state;
        }
		
		public function toString():String {
			return new String ("GameModel.  Implementation:  " + this.implementation);
		}		
	}
}
