package mx.ecosur.multigame.component
{
    import mx.collections.ArrayCollection;
    import mx.containers.Accordion;
    import mx.controls.Button;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.entity.GamePlayer;
    

	public class PlayersViewer extends Accordion
	{
        private var _players:ArrayCollection; 
        private var _selectedPlayer:PlayerInfo;
        
        /**
         * Default constructor 
         */
        public function PlayersViewer(){
            super();
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
        
        public function setTurn(player:GamePlayer):void{
            var pi:PlayerInfo;
            for (var i:int = 0; i < getChildren().length; i++){
                pi = PlayerInfo(getChildAt(i))
                if (pi.gamePlayer.id == player.id){
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
        public function getPlayerButton(player:GamePlayer):Button{
            
            var pi:PlayerInfo;
            var btn:Button;
            for (var i:int = 0; i < getChildren().length; i++){
                pi = PlayerInfo(getChildAt(i))
                if (pi.gamePlayer.id == player.id){
                    btn = getHeaderAt(i);
                    return btn;
                }
            }
            return null;
        }
        
        /*
         * Updates the players and opens the player whose turn it is. 
         */
        private function updatePlayers():void{
            
            var pi:PlayerInfo;
            var btn:Button;
            var gamePlayer:GamePlayer;
            var i:int;
            
            for (i = 0; i < _players.length; i++){
                
                // Create the player information
                gamePlayer = GamePlayer(_players[i]);
                if (getChildren().length > i) {
                    pi = PlayerInfo(getChildAt(i));
                }else{
                    pi = new PlayerInfo();
                    addChild(pi);
                }
                pi.gamePlayer = gamePlayer;
                
                // Create button header
                var label:String = gamePlayer.player.name; 
                btn = getHeaderAt(i);
                btn.label = label;
                btn.setStyle("icon", Color.getCellIcon(gamePlayer.color));
                btn.setStyle("paddingBottom", 5);
                btn.setStyle("paddingTop", 5);
                
                // If player has turn highlight and select its info
                if (gamePlayer.turn){
                    selectPlayer(pi);
                }
            }
            
            // Remove extra children that are no longer used
            while (i < getChildren().length){
                removeChildAt(i);   
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
    }       
}