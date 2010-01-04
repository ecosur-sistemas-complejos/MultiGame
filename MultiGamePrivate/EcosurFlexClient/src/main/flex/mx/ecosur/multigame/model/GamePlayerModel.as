package mx.ecosur.multigame.model
{

    import mx.ecosur.multigame.entity.GamePlayer;

	[RemoteClass (alias="mx.ecosur.multigame.model.GamePlayer")]
	public class GamePlayerModel
	{

        private var _gamePlayer:GamePlayer;
        private var _id:int;
        private var _turn:Boolean;
        private var _game:GameModel;

        public function GamePlayerModel() {
            super();
        }

        public function get id ():int {
            return _id;
        }

        public function set id (id:int):void {
            _id = id;
        }

        public function get implementation ():GamePlayer {
            return _gamePlayer;
        }

        public function set implementation (gamePlayer:GamePlayer):void {
            _gamePlayer = gamePlayer;
        }
    }
}