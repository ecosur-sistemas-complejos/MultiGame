package mx.ecosur.multigame.pente {
	
	import mx.collections.ArrayCollection;
	import mx.containers.Accordion;
	import mx.controls.Button;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.GamePlayer;
	import mx.ecosur.multigame.helper.Color;
	
	/**
	 * Visual component representing the players that are in the current
	 * game.
	 * 
	 */
	public class PentePlayersViewer extends Accordion {
		
		private var _players:ArrayCollection; 
		
		/**
		 * Default constructor 
		 */
		public function PentePlayersViewer(){
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
		
		
		/**
		 * Returns the button for this player.
		 * 
		 * @param player
		 */
		public function getPlayerButton(player:GamePlayer):Button{
			
			var ppi:PentePlayerInfo;
			var btn:Button;
			for (var i:int = 0; i < getChildren().length; i++){
				ppi = PentePlayerInfo(getChildAt(i))
				if (ppi.gamePlayer.id == player.id){
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
			
			var ppi:PentePlayerInfo;
			var btn:Button;
			var cell:Cell;
			var gamePlayer:GamePlayer;
			var i:int;
			
			for (i = 0; i < _players.length; i++){
				
				// Create the player information
				gamePlayer = GamePlayer(_players[i]);
				if (getChildren().length > i) {
					ppi = PentePlayerInfo(getChildAt(i));
				}else{
					ppi = new PentePlayerInfo();
					addChild(ppi);
				}
				ppi.gamePlayer = gamePlayer;
				
				// Create button header
				btn = getHeaderAt(i);
				btn.label = gamePlayer.player.name;
				btn.setStyle("icon", Color.getCellIcon(gamePlayer.color));
				btn.setStyle("paddingBottom", 5);
				btn.setStyle("paddingTop", 5);
				
				// If player has turn highlight and select its info
				if (gamePlayer.turn){
					var fillColor:uint;
					fillColor = Color.findIntermediateColor(Color.getColorCode(gamePlayer.color), 0xffffff, 0.3);
					btn.setStyle("fillColors", [fillColor, 0xffffff]);
					btn.setStyle("fillAlphas", [1, 1, 1]);
					btn.setStyle("borderColor", fillColor);
					selectedChild = ppi;
				}else{
					btn.setStyle("fillColors", [0xE6EEEE, 0xFFFFFF]);
					btn.setStyle("fillAlphas", [0.6, 0.4]);
					btn.setStyle("borderColor", 0xAAB3B3);
				}
			}
			
			// Remove extra children that are no longer used
			while (i < getChildren().length){
				removeChildAt(i);	
			}
		}
		
	}
		
}