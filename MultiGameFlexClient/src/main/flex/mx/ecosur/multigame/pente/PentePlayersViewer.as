package mx.ecosur.multigame.pente {
	
	import mx.collections.ArrayCollection;
	import mx.containers.Accordion;
	import mx.controls.Button;
	import mx.ecosur.multigame.enum.Color;
	import mx.ecosur.multigame.pente.entity.PentePlayer;
	
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
		
		public function setTurn(player:PentePlayer):void{
			var ppi:PentePlayerInfo;
			for (var i:int = 0; i < getChildren().length; i++){
				ppi = PentePlayerInfo(getChildAt(i))
				if (ppi.pentePlayer.id == player.id){
					selectPlayer(ppi);
					return;
				}
			}
		}
		
		
		/**
		 * Returns the button for this player.
		 * 
		 * @param player
		 */
		public function getPlayerButton(player:PentePlayer):Button{
			
			var ppi:PentePlayerInfo;
			var btn:Button;
			for (var i:int = 0; i < getChildren().length; i++){
				ppi = PentePlayerInfo(getChildAt(i))
				if (ppi.pentePlayer.id == player.id){
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
			var pentePlayer:PentePlayer;
			var i:int;
			
			for (i = 0; i < _players.length; i++){
				
				// Create the player information
				pentePlayer = PentePlayer(_players[i]);
				if (getChildren().length > i) {
					ppi = PentePlayerInfo(getChildAt(i));
				}else{
					ppi = new PentePlayerInfo();
					addChild(ppi);
				}
				ppi.pentePlayer = pentePlayer;
				
				// Create button header
				var label:String = pentePlayer.player.name + " (" + Color.getTeamName(pentePlayer.color) + ")"; 
				btn = getHeaderAt(i);
				btn.label = label;
				btn.setStyle("icon", Color.getCellIcon(pentePlayer.color));
				btn.setStyle("paddingBottom", 5);
				btn.setStyle("paddingTop", 5);
				
				// If player has turn highlight and select its info
				if (pentePlayer.turn){
					selectPlayer(ppi);
				}
			}
			
			// Remove extra children that are no longer used
			while (i < getChildren().length){
				removeChildAt(i);	
			}
		}
		
		private function selectPlayer(ppi:PentePlayerInfo):void{
			
			//deselect selected
			var btn:Button = getHeaderAt(selectedIndex);
			btn.setStyle("fillColors", [0xE6EEEE, 0xFFFFFF]);
			btn.setStyle("fillAlphas", [0.6, 0.4]);
			btn.setStyle("color", 0x0B333C);
			btn.setStyle("borderColor", 0xAAB3B3);
			
			//select new
			btn = getHeaderAt(getChildIndex(ppi));
			btn.setStyle("fillColors", [0xFF3300, 0xFF6600]);
			btn.setStyle("fillAlphas", [1, 1]);
			btn.setStyle("color", 0xFFFFFF);
			btn.setStyle("borderColor", 0xFF3300);
			
			selectedChild = ppi;
		}
		
		
	}
		
}