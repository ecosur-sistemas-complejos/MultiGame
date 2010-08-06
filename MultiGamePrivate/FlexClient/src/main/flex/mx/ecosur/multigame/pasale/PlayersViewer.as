package mx.ecosur.multigame.oculto
{
    import mx.collections.ArrayCollection;
    import mx.containers.Accordion;
    import mx.controls.Button;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.oculto.entity.OcultoPlayer;
    

	public class PlayersViewer extends Accordion
	{
		private var _currentPlayer:GamePlayer;
        private var _players:ArrayCollection;         
        private var _selectedPlayer:PlayerInfo;
        private var _gi:GameInfo;
        
        /**
         * Default constructor 
         */
        public function PlayersViewer(){
            super();
        }
        
        public function set currentPlayer (currentPlayer:GamePlayer):void 
        {
        	_currentPlayer = currentPlayer;
        }
        
        public function get currentPlayer ():GamePlayer {
            return _currentPlayer;	
        }
        
        /**
         * Sets players
         *  
         * @param players
         */
        public function set players(players:ArrayCollection):void{
            _players = players;
            updatePlayers();
        }
        
        public function setTurn(player:TablonPlayer):void{
            var pi:PlayerInfo;
            for (var i:int = 0; i < getChildren().length; i++){
                pi = PlayerInfo(getChildAt(i))
                if (pi.ocultoPlayer.id == player.id){
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
        public function getPlayerButton(player:TablonPlayer):Button{
            
            var pi:PlayerInfo;
            var btn:Button;
            for (var i:int = 1; i < getChildren().length; i++){
                pi = PlayerInfo(getChildAt(i))
                if (pi.ocultoPlayer.id == player.id){
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
            _gi.players = _players;                                
            
            /* Setup all player info tabs */
            for (var i:int = 0; i < _players.length; i++){
                var pi:PlayerInfo = null;
                var btn:Button = null;
                var manPlayer:TablonPlayer = null;
            	
                manPlayer = TablonPlayer(_players[i]);
                
                for (var j:int = 0; j < getChildren().length; j++) {
                	if (getChildAt(j) is PlayerInfo) {
                		var info:PlayerInfo = PlayerInfo(getChildAt(j));
                		if (info.ocultoPlayer.id == manPlayer.id) {
                		  pi = info;
                		  break;
                		}
                	}
                }                
                
                // Create the player information if necessary 
                if (pi == null) {
                    pi = new PlayerInfo();                    
                    addChild(pi);
                    pi.ocultoPlayer = manPlayer;                       
                }
                
                pi.ocultoPlayer = manPlayer;
                pi.update();        
                
                var label:String;                                                           
                
                for (j = 0; j < getChildren().length; j++) {
                    if (getChildAt(j) is PlayerInfo) {
                        info = PlayerInfo(getChildAt(j));
                        if (info.ocultoPlayer.id == manPlayer.id) {
			                // Create button header if required
			                btn = getHeaderAt(j)
			                label = manPlayer.registrant.name; 
			                btn.label = label;
			                btn.setStyle("icon", Color.getCellIcon(manPlayer.color));
			                btn.setStyle("paddingBottom", 5);
			                btn.setStyle("paddingTop", 5);                           	
                        }
                    } else {
                        btn = getHeaderAt(j)
				        label = "Game Info";
				        btn.label = label;
				        btn.setStyle("icon", Color.getCellIcon(Color.BLACK));
				        btn.setStyle("paddingBottom", 5);
				        btn.setStyle("paddingTop", 5);                         	
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
        	var btn:Button = new Button();
        	
        	super.createChildren();
        	_gi = new GameInfo();
        	addChild(_gi);
        }   
    }       
}