/*
 * GameDAO.as
 * 
 * Class used for transferring game information (id, creation date, gametype, player names, and gamestate to the
 * Lobby for display.
 * 
 * Solves inhertiance issues between BLAZEDS and the following classes: 
 *  GridGame, GenteGame and ManantialesGame.
 * 
 * @author awaterma@ecosur.mx
 * 
 */
package mx.ecosur.multigame.dao {

    import mx.collections.ArrayCollection;
    import mx.ecosur.multigame.entity.Game;
    import mx.ecosur.multigame.entity.GamePlayer;

    [RemoteClass (alias="mx.ecosur.multigame.flexClient.dao.GameDAO")]
    public class GameDAO {
        
        private var _gameId:int;
        
        private var _creationDate:Date;
        
        private var _gameType:String;
        
        private var _players:ArrayCollection;
    
        private var _status:String;

        public function GameDAO() {
            super();
        }
        
        public function initialize (game:Game):void {
            _gameId = game.id;
            _creationDate = game.created;
            _gameType = game.gameType;
            _status = game.state;
            _players = new ArrayCollection();
            for (var i:int = 0; i < game.players.length; i++) {
                var gp:GamePlayer = GamePlayer (game.players [ i ]);
                _players.addItem(gp.name);
            }
        }

        public function get gameId():int {
            return _gameId;
        }

        public function set gameId(value:int):void {
            _gameId = value;
        }

        public function get creationDate():Date {
            return _creationDate;
        }

        public function set creationDate(value:Date):void {
            _creationDate = value;
        }

        public function get gameType():String {
            return _gameType;
        }

        public function set gameType(value:String):void {
            _gameType = value;
        }

        public function get players():ArrayCollection {
            return _players;
        }

        public function set players(value:ArrayCollection):void {
            _players = value;
        }

        public function get status():String {
            return _status;
        }

        public function set status(value:String):void {
            _status = value;
        }
    }
}
