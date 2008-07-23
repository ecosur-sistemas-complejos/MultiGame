package mx.ecosur.multigame.pente{
	
	import flash.geom.Point;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.core.UIComponent;
	import mx.ecosur.multigame.component.BoardCell;
	import mx.ecosur.multigame.component.Token;
	import mx.ecosur.multigame.entity.Move;
	import mx.ecosur.multigame.enum.Color;
	import mx.ecosur.multigame.pente.entity.PenteGame;
	import mx.ecosur.multigame.pente.entity.PenteMove;
	import mx.ecosur.multigame.pente.entity.PentePlayer;
	import mx.effects.AnimateProperty;
	import mx.events.DynamicEvent;
	import mx.events.EffectEvent;
	import mx.messaging.messages.ErrorMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;
	

	/**
	 * Replays a game of pente.
	 */
	public class PenteGameReplayController {
		
		// Visual components
		private var _board:PenteBoard;
		private var _playersViewer:PentePlayersViewer;
		private var _moveViewer:PenteMoveViewer;
		private var _animateLayer:UIComponent;
		
		// Data objects
		private var _game:PenteGame;
		private var _moves:ArrayCollection; //all moves made in the game
		private var _selectedMoveInd:Number; //index of selected move in _moves
		
		// Server objects
		private var _gameService:RemoteObject;
		
		// constants
		private static const MESSAGING_DESTINATION_NAME:String = "multigame-destination";
		private static const GAME_SERVICE_DESTINATION_NAME:String = "gameService";
		private static const GAME_SERVICE_GET_GAME_OP:String = "getGame";
		private static const GAME_SERVICE_GET_PLAYERS_OP:String = "getPlayers";
		private static const GAME_SERVICE_GET_MOVES_OP:String = "getMoves";
		
		/**
		 * Default constructor. 
		 * 
		 */
		public function PenteGameReplayController(board:PenteBoard, playersViewer:PentePlayersViewer, moveViewer:PenteMoveViewer, animateLayer:UIComponent){
			super();
			
			//set private references
			_board = board;
			_playersViewer = playersViewer;
			_moveViewer = moveViewer;
			_animateLayer = animateLayer;
			_moves = new ArrayCollection();
			
			//initialize game service remote object
			_gameService = new RemoteObject();
			_gameService.destination = GAME_SERVICE_DESTINATION_NAME;
			_gameService.addEventListener(ResultEvent.RESULT, gameServiceResultHandler);
			_gameService.addEventListener(FaultEvent.FAULT, gameServiceFaultHandler);
			
			//initialize the move viewer
			_moveViewer.addEventListener(PenteMoveViewer.MOVE_EVENT_GOTO_MOVE, gotoMoveHandler);
			_moveViewer.board = _board;
			
			//get the game
			var call:Object = _gameService.getGame("PENTE");
			call.operation = GAME_SERVICE_GET_GAME_OP;
		}
		
		
		/*
		 * Game service result handler. Depending on the type of call 
		 * different actions are taken.
		 */
		private function gameServiceResultHandler(event:ResultEvent):void{
			var call:Object = event.token;
			switch (call.operation){
				case GAME_SERVICE_GET_GAME_OP:
					_game = PenteGame(event.result);
					var callPlayers:Object = _gameService.getPlayers();
					callPlayers.operation = GAME_SERVICE_GET_PLAYERS_OP;
					break;
				case GAME_SERVICE_GET_PLAYERS_OP:
					var pentePlayer:PentePlayer = PentePlayer(ArrayCollection(event.result)[0]);
					_playersViewer.players = ArrayCollection(event.result);
					var callMoves:Object = _gameService.getMoves();
					callMoves.operation = GAME_SERVICE_GET_MOVES_OP;
					break;
				case GAME_SERVICE_GET_MOVES_OP:
					_moves = ArrayCollection(event.result)
					_moveViewer.initFromMoves(_moves);
					_selectedMoveInd = -1;
					gotoMove(PenteMove(_moves[0]));
					break;
			}
		}
		
		/*
		 * Handles faults from game service calls 
		 */
		private function gameServiceFaultHandler(event:FaultEvent):void{
			var errorMessage:ErrorMessage = ErrorMessage(event.message);
			Alert.show(event.fault.faultString, "Server Error");
		}
		
		private function gotoMoveHandler(event:DynamicEvent):void{
			
			var move:PenteMove = PenteMove(event.move);
			gotoMove(move);
		}
		
		private function gotoMove(move:PenteMove):void{

			if(_selectedMoveInd > -1 && move.id < PenteMove(_moves[_selectedMoveInd]).id){
				do{
					undoMove(PenteMove(_moves[_selectedMoveInd]));
					_selectedMoveInd --;					
				}while(move.id < PenteMove(_moves[_selectedMoveInd]).id && _selectedMoveInd > -1);
			}else if ((_selectedMoveInd < 0 || move.id > PenteMove(_moves[_selectedMoveInd]).id) && _selectedMoveInd < _moves.length){
				do{
					doMove(PenteMove(_moves[_selectedMoveInd + 1]));
					_selectedMoveInd ++;
				}while(move.id > PenteMove(_moves[_selectedMoveInd]).id && _selectedMoveInd < _moves.length);
			}
		}
		
		public function gotoFirstMove():void{
			gotoMove(PenteMove(_moves[0]));
		}
		
		public function gotoPreviousMove():void{
			if (_selectedMoveInd > 0){
				gotoMove(PenteMove(_moves[_selectedMoveInd - 1]));
			}
		}
		
		public function gotoNextMove():void{
			if (_selectedMoveInd < _moves.length - 2){
				gotoMove(PenteMove(_moves[_selectedMoveInd + 1]));
			}
		}
		
		public function gotoLastMove():void{
			gotoMove(PenteMove(_moves[_moves.length - 1]));
		}
		
		/*
		 * Animates a move
		 */
		private function doMove(move:Move):void{
			
			//define origin
			var startPoint:Point;
			var startSize:Number;
			var playerBtn:Button = _playersViewer.getPlayerButton(PentePlayer(move.player));
			startPoint = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y + Color.getCellIconSize() / 2 + 5);
			startPoint = _playersViewer.localToGlobal(startPoint);
			startPoint = _animateLayer.globalToLocal(startPoint);
			startSize = Color.getCellIconSize();
						
			//define destination
			var boardCell:BoardCell = _board.getBoardCell(move.destination.column, move.destination.row);
			var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
			var endSize:Number = _board.tokenSize;
			endPoint = boardCell.localToGlobal(endPoint);
			endPoint = _animateLayer.globalToLocal(endPoint);
			
			//create new token
			var token:Token = new Token();
			token.cell = move.destination;
			token.width = endSize;
			token.height = endSize;
			_animateLayer.addChild(token);
						
			//define motion animation
			var apX:AnimateProperty = new AnimateProperty(token);
			apX.fromValue = startPoint.x;
			apX.toValue = endPoint.x;
			apX.duration = 1000;
			apX.property = "x";
			var apY:AnimateProperty = new AnimateProperty(token);
			apY.fromValue = startPoint.y;
			apY.toValue = endPoint.y;
			apY.duration = 1000;
			apY.property = "y";
			apY.addEventListener(EffectEvent.EFFECT_END, endDoMove);
			
			//define size animation
			var apXScale:AnimateProperty = new AnimateProperty(token);
			apXScale.property = "scaleX";
			apXScale.fromValue = startSize / endSize;
			apXScale.toValue = 1;
			apXScale.duration = 1000;
			var apYScale:AnimateProperty = new AnimateProperty(token);
			apYScale.property = "scaleY";
			apYScale.fromValue = startSize / endSize;
			apYScale.toValue = 1;
			apYScale.duration = 1000;
			
			//start effect
			apX.play();
			apY.play();
			apXScale.play();
			apYScale.play();
		}
		
		private function endDoMove(event:EffectEvent):void{
			
			var token:Token = Token(AnimateProperty(event.currentTarget).target);
			var boardCell:BoardCell = _board.getBoardCell(token.cell.column, token.cell.row);
			var move:PenteMove = PenteMove(_moves[_selectedMoveInd]);
			_animateLayer.removeChild(token);
			boardCell.token = token;
			_moveViewer.selectedMove = move;
			_playersViewer.setTurn(PentePlayer(move.player));
		}
		
		private function undoMove(move:Move):void{
			
			//define origin
			var boardCell:BoardCell = _board.getBoardCell(move.destination.column, move.destination.row);
			var startPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
			var startSize:Number = _board.tokenSize;
			startPoint = boardCell.localToGlobal(startPoint);
			startPoint = _animateLayer.globalToLocal(startPoint);
			
			//define destination
			var endPoint:Point;
			var endSize:Number;
			var playerBtn:Button = _playersViewer.getPlayerButton(PentePlayer(move.player));
			endPoint = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y + Color.getCellIconSize() / 2 + 5);
			endPoint = _playersViewer.localToGlobal(endPoint);
			endPoint = _animateLayer.globalToLocal(endPoint);
			endSize = Color.getCellIconSize();
						
			//create new token
			var token:Token = new Token();
			token.cell = move.destination;
			token.width = endSize;
			token.height = endSize;
			boardCell.token = null;
			_animateLayer.addChild(token);

			//define motion animation
			var apX:AnimateProperty = new AnimateProperty(token);
			apX.fromValue = startPoint.x;
			apX.toValue = endPoint.x;
			apX.duration = 1000;
			apX.property = "x";
			var apY:AnimateProperty = new AnimateProperty(token);
			apY.fromValue = startPoint.y;
			apY.toValue = endPoint.y;
			apY.duration = 1000;
			apY.property = "y";
			apY.addEventListener(EffectEvent.EFFECT_END, endUndoMove);
			
			//define size animation
			var apXScale:AnimateProperty = new AnimateProperty(token);
			apXScale.property = "scaleX";
			apXScale.fromValue = startSize / endSize;
			apXScale.toValue = 1;
			apXScale.duration = 1000;
			var apYScale:AnimateProperty = new AnimateProperty(token);
			apYScale.property = "scaleY";
			apYScale.fromValue = startSize / endSize;
			apYScale.toValue = 1;
			apYScale.duration = 1000;
			
			//start effect
			apX.play();
			apY.play();
			apXScale.play();
			apYScale.play();
		}
		
		private function endUndoMove(event:EffectEvent):void{
			
			var token:Token = Token(AnimateProperty(event.currentTarget).target);
			var move:PenteMove = PenteMove(_moves[_selectedMoveInd]);
			_animateLayer.removeChild(token);
			_moveViewer.selectedMove = move;
			_playersViewer.setTurn(PentePlayer(move.player));
		}
	}
}