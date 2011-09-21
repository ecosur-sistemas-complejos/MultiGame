package mx.ecosur.multigame.manantiales
{
    import flash.utils.Dictionary;

    import mx.containers.Accordion;
    import mx.controls.Button;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.entity.manantiales.ManantialesGame;
    import mx.ecosur.multigame.entity.manantiales.ManantialesPlayer;
    import mx.resources.IResourceManager;
    import mx.resources.ResourceManager;

    [ResourceBundle("Manantiales")]
    public class PlayersViewer extends Accordion
	{
		private var _currentPlayer:GamePlayer;
        private var _game:ManantialesGame;
        private var _selectedPlayer:PlayerInfo;
        private var _gi:GameInfo;
        private var _map:Dictionary;

        /**
         * Default constructor 
         */
        public function PlayersViewer(){
            super();
        }
        
        public function set game (game:ManantialesGame):void {
            _game = game;
            _gi.game = game;
            updatePlayers();
        }
        
        public function set currentPlayer (currentPlayer:GamePlayer):void 
        {
        	_currentPlayer = currentPlayer;
        }
        
        public function get currentPlayer ():GamePlayer {
            return _currentPlayer;	
        }
        
        public function setTurn(player:ManantialesPlayer):void{
            var pi:PlayerInfo;
            for (var i:int = 0; i < getChildren().length; i++){
                if (getChildAt(i) instanceof GameInfo)
                    continue;
                pi = PlayerInfo(getChildAt(i));

                if (pi.manantialesPlayer.id == player.id){
                    selectPlayer(pi);
                    return;
                }
            }
        }
        
        
        /**
         * Returns the button for this player.
         * 
         * @param player
         */
        public function getPlayerButton(player:ManantialesPlayer):Button{
            
            var pi:PlayerInfo;
            var btn:Button;
            for (var i:int = 1; i < getChildren().length; i++){
                pi = PlayerInfo(getChildAt(i))
                if (pi.manantialesPlayer.id == player.id){
                    btn = getHeaderAt(i);
                    return btn;
                }
            }
            return null;
        }
        
        /*
         * Updates the players and opens the player whose turn it is. 
         */
        public function updatePlayers():void{
            /* Set the game players into the game info */
            _gi.game = _game;

            /* Setup all player info tabs */
            for (var i:int = 0; i < _game.players.length; i++){
                var pi:PlayerInfo = null;
                var btn:Button = null;
                var manPlayer:ManantialesPlayer = ManantialesPlayer(_game.players[i]);

                /* Ghost updates are ignored */
                if (manPlayer.id == 0)
                    continue;
                
                for (var j:int = 0; j < getChildren().length; j++) {
                    if (getChildAt(j) is PlayerInfo) {
                        var info:PlayerInfo = PlayerInfo(getChildAt(j));
                        if (info.manantialesPlayer.id == manPlayer.id) {
                          pi = info;
                          break;
                        }
                    }
                }                
                
                // Create the player information if necessary 
                if (pi == null) {
                    pi = new PlayerInfo();                    
                    addChild(pi);
                }
                
                pi.manantialesPlayer = manPlayer;
                pi.update();        
                
                var label:String;                                                           
                
                for (j = 0; j < getChildren().length; j++) {
                    if (getChildAt(j) is PlayerInfo) {
                        info = PlayerInfo(getChildAt(j));
                        if (info.manantialesPlayer.id == manPlayer.id) {
                            // Create button header if required
                            btn = getHeaderAt(j);
                            label = manPlayer.name;
                            btn.label = label;
                            btn.setStyle("paddingBottom", 5);
                            btn.setStyle("paddingTop", 5);
                            btn.setStyle("icon", Color.getCellIcon(manPlayer.color));                            
                        }
                    } else {
                        btn = getHeaderAt(j)
                        label = resourceManager.getString("Manantiales","manantiales.panel.info.score.title");
                        btn.label = label;
                        btn.setStyle("paddingBottom", 5);
                        btn.setStyle("paddingTop", 5);
                        btn.setStyle("center", true);
                    }
                }

                
                // If player has turn highlight and select its info
                if (manPlayer.turn) {
                    selectPlayer(pi);
                }
            }
        }
        
        private function selectPlayer(pi:PlayerInfo):void{
            
            //deselect selected
            if (_selectedPlayer){
                var btn:Button = getHeaderAt(getChildIndex(_selectedPlayer));
                btn.setStyle("fillColors", [0xE6EEEE, 0xFFFFFF]);
                btn.setStyle("fillAlphas", [0.6, 0.4]);
                btn.setStyle("color", 0x0B333C);
                btn.setStyle("borderColor", 0xAAB3B3);
            }
            
            //select new
            btn = getHeaderAt(getChildIndex(pi));
            btn.setStyle("fillColors", [0xFF3300, 0xFF6600]);
            btn.setStyle("fillAlphas", [1, 1]);
            btn.setStyle("color", 0xFFFFFF);
            btn.setStyle("borderColor", 0xFF3300);
            
            _selectedPlayer = pi;
            selectedChild = pi;
        }
        
        override protected function createChildren():void {
        	super.createChildren();
        	_gi = new GameInfo();
        	addChild(_gi);
        }
    }       
}
