/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.gente{
    
    import flash.events.MouseEvent;
    import flash.geom.Point;
    import flash.media.Sound;
    import flash.media.SoundChannel;

    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.controls.Button;
    import mx.core.DragSource;
    import mx.core.IFlexDisplayObject;
    import mx.core.UIComponent;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.component.ChatPanel;
    import mx.ecosur.multigame.component.GameStatus;
    import mx.ecosur.multigame.component.SoundAssets;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.component.TokenStore;
    import mx.ecosur.multigame.entity.Cell;
    import mx.ecosur.multigame.entity.ChatMessage;
    import mx.ecosur.multigame.entity.Game;
    import mx.ecosur.multigame.entity.GameGrid;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.entity.Move;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.CooperatiionQualifier;
    import mx.ecosur.multigame.enum.ExceptionType;
    import mx.ecosur.multigame.enum.GameEvent;
    import mx.ecosur.multigame.enum.MoveStatus;
    import mx.ecosur.multigame.gente.entity.BeadString;
    import mx.ecosur.multigame.gente.entity.GenteGame;
    import mx.ecosur.multigame.gente.entity.GenteMove;
    import mx.ecosur.multigame.gente.entity.GentePlayer;
    import mx.ecosur.multigame.gente.entity.StrategyPlayer;
    import mx.ecosur.multigame.util.MessageReceiver;
    import mx.effects.AnimateProperty;
    import mx.events.CloseEvent;
    import mx.events.DragEvent;
    import mx.events.DynamicEvent;
    import mx.events.EffectEvent;
    import mx.managers.DragManager;
    import mx.messaging.Producer;
    import mx.messaging.messages.AsyncMessage;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;
    import mx.resources.IResourceManager;
    import mx.resources.ResourceManager;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.RemoteObject;

    /**
     * Represents a game of gente. Contains a gente board and cell store
     * and controls the main flow of control for the game.
     */
    [ResourceBundle("StringsBundle")]
    public class GenteGameController {

        private var resourceManager:IResourceManager = ResourceManager.getInstance();
        
        // visual components
        private var _board:GenteBoard;
        private var _chatPanel:ChatPanel;
        private var _playersViewer:GentePlayersViewer;
        private var _tokenStore:TokenStore;
        private var _moveViewer:GenteMoveViewer;
        private var _gameStatus:GameStatus;
        private var _animateLayer:UIComponent;

        protected var _sndChannel:SoundChannel;
        
        // data objects
        private var _gameId:int;
        private var _currentPlayer:GamePlayer;
        private var _players:ArrayCollection;
        private var _gameGrid:GameGrid;
        private var _game:GenteGame;
        private var _moves:ArrayCollection; 
        private var _selectedMoveInd:Number;
        private var _winners:ArrayCollection;
        
        // server objects
        private var _gameService:RemoteObject;
        private var _msgReceiver:MessageReceiver;
        private var _msgProducer:Producer;
        
        // flags
        private var _isMoving:Boolean;
        private var _isTurn:Boolean;
        private var _executingMove:GenteMove;
        private var _isEnded:Boolean;

        // constants
        private static const TOKEN_STORE_MIN_WIDTH:int = 150;
        private static const TOKEN_STORE_MAX_WIDTH:int = 300;
        private static const N_TOKENS_IN_STORE:int = 50;
        private static const MESSAGING_DESTINATION_NAME:String = "multigame-destination";
        private static const GAME_SERVICE_DESTINATION_NAME:String = "gameService";
        private static const GAME_SERVICE_GET_GRID_OP:String = "getGameGrid";
        private static const GAME_SERVICE_GET_PLAYERS_OP:String = "getPlayers";
        private static const GAME_SERVICE_GET_MOVES_OP:String = "getMoves";
        private static const GAME_SERVICE_UPDATE_MOVE_OP:String = "updateMove";
        private static const GAME_SERVICE_DO_MOVE_OP:String = "doMove";
        
        /**
         * Constructor 
         */
        public function GenteGameController(currentGame:Game, currentPlayer:GamePlayer,
            board:GenteBoard, chatPanel:ChatPanel, playersViewer:GentePlayersViewer,
            tokenStore:TokenStore, gameStatus:GameStatus, moveViewer:GenteMoveViewer, animateLayer:UIComponent)
       {
            super();

            _msgProducer = new Producer();
            _msgProducer.destination = "multigame-destination";           
            
            // set private references
            _gameId = currentGame.id;
            _currentPlayer = currentPlayer;
            _board = board;
            _chatPanel = chatPanel;
            _tokenStore = tokenStore;
            _gameStatus = gameStatus;
            _playersViewer = playersViewer;
            _moveViewer = moveViewer;
            _animateLayer = animateLayer;
            _moves = new ArrayCollection();
            _isMoving = false;  
            _isEnded = false;
            
            // initialize game service remote object
            _gameService = new RemoteObject();
            _gameService.destination = GAME_SERVICE_DESTINATION_NAME;
            _gameService.addEventListener(ResultEvent.RESULT, gameServiceResultHandler);
            _gameService.addEventListener(FaultEvent.FAULT, gameServiceFaultHandler);
            
            // initialize message receiver
            _msgReceiver = new MessageReceiver(MESSAGING_DESTINATION_NAME, _gameId);
            _msgReceiver.addEventListener(MessageReceiver.PROCESS_MESSAGE, processMessage);
            
            // initialize the board
            _board.dragEnterHandler = dragEnterBoardCell;
            _board.dragDropHandler = dragDropCell;
            _board.dragExitHandler = dragExitCell;  
            
            // initialize token store
            _tokenStore.startMoveHandler = startMove;
            _tokenStore.endMoveHandler = endMove;
            _tokenStore.active = false;
            
            // initialize game status
            _gameStatus.showMessage(resourceManager.getString("StringsBundle", "gente.welcome") +
                currentPlayer.registrant.name + "!\n\n" +
                    " " + resourceManager.getString("StringsBundle", "gente.identify") + " " + 
                Color.getColorDescription(currentPlayer.color), 
                    Color.getColorCode(currentPlayer.color));
    
            // initialize the move viewer
            _moveViewer.addEventListener(GenteMoveViewer.MOVE_EVENT_GOTO_MOVE, gotoMove);
            _moveViewer.board = _board;
            
            // get the game grid, players and moves
            var callGrid:Object = _gameService.getGameGrid(_gameId);
            callGrid.operation = GAME_SERVICE_GET_GRID_OP;
            var callPlayers:Object = _gameService.getPlayers(_gameId);
            callPlayers.operation = GAME_SERVICE_GET_PLAYERS_OP;
            var callMoves:Object = _gameService.getMoves(_gameId);
            callMoves.operation = GAME_SERVICE_GET_MOVES_OP;

           // send out a player change message to kick off any JMS
           var playerchange:IMessage = new AsyncMessage();
           playerchange.headers.GAME_ID = _gameId;
	       playerchange.headers.GAME_EVENT = GameEvent.PLAYER_CHANGE;
           playerchange.body = _game;
           _msgProducer.send (playerchange)
        }
        
        public function set isTurn(isTurn:Boolean):void{
            if(_isTurn != isTurn){
                _isTurn = isTurn;
                if (_isTurn){
                    initTurn();
                }else{
                    endTurn();
                }
            }
        }
        
        /**
         * Updates the list of players. Also updates the chat panel and 
         * players viewer components.
         *  
         * @param players the new list of players
         */
        public function updatePlayers(game:GenteGame):void{

            /* Force compilation of StrategyPlayer in .swf file - ugly code */
            var strategyPlayer:StrategyPlayer;

            _game = game;

            var gamePlayer:GamePlayer;
            for (var i:int = 0; i < game.players.length; i++){
                gamePlayer = GamePlayer(game.players[i]);
                if (gamePlayer.registrant.id == _currentPlayer.registrant.id){
                    _currentPlayer = gamePlayer;
                    _chatPanel.currentPlayer = _currentPlayer;
                    this.isTurn = _currentPlayer.turn;
                }

                if (gamePlayer.turn){
                    if (gamePlayer.id == _currentPlayer.id){
                        _gameStatus.showMessage(resourceManager.getString(
                                "StringsBundle", "gente.currentplayer.turn"),
                                Color.getColorCode(_currentPlayer.color));
                    }else{
                        _gameStatus.showMessage(gamePlayer.registrant.name +
                            " " + resourceManager.getString(
                                "StringsBundle", "gente.tomove"),
                                Color.getColorCode(gamePlayer.color));
                    }
                }
            }
            
            _playersViewer.players = game.players;
            _players = game.players;
        }
        
        /*
         * Called when game begins 
         */
        private function begin():void{
            _gameStatus.showMessage(resourceManager.getString(
                    "StringsBundle", "gente.start.message"), 0x00000);
        }
        
        /*
         * Called when game ends 
         */
        private function end():void{
            
            if(_isEnded){
                return;
            }
            
            // remove the token store
            _tokenStore.active = false;
            var sound:Sound = null;

            // prepare message for winners
            var msg:String = "";
            var color:uint;
            var gentePlayer:GentePlayer = GentePlayer(_winners[0]);
            var _isSoloWin:Boolean = _winners.length == 1;
            if (_isSoloWin){
                msg = gentePlayer.registrant.name + " " + resourceManager.getString
                        ("StringsBundle", "gente.has.won");
                color = Color.getColorCode(gentePlayer.color);
                if (gentePlayer.color == _currentPlayer.color)
                    sound = SoundAssets.approval;
                else
                    sound = SoundAssets.failure;
            }else{
                msg = Color.getTeamName(gentePlayer.color) + " " 
                        + resourceManager.getString("StringsBundle", "gente.team.has.won");
                color = 0x000000;
                if (Color.getTeamName(gentePlayer.color) == Color.getTeamName(_currentPlayer.color))
                    sound = SoundAssets.approval;
                else
                    sound = SoundAssets.failure;
            }
            _gameStatus.showMessage(msg, 0x00000);
            _gameStatus.active = false;
            
            // blink and select the winning board cells and tokens
            var beadString:BeadString;
            var cell:Cell;
            var boardCell:BoardCell;
            if (_isSoloWin){
                for (var j:int = 0; j < gentePlayer.trias.length; j++){
                    beadString = BeadString(gentePlayer.trias[j]);
                    for (var k:Number = 0; k < beadString.beads.length; k++){
                        cell = Cell(beadString.beads[k]);
                        boardCell = _board.getBoardCell(cell.column, cell.row); 
                        boardCell.token.blink(10);
                        boardCell.select(cell.colorCode);
                    }
                }
            }else{
                for (var m:int = 0; m < gentePlayer.tesseras.length; m++){
                    beadString = BeadString(gentePlayer.tesseras[m]);
                    for (var n:Number = 0; n < beadString.beads.length; n++){
                        cell = Cell(beadString.beads[n]);
                        boardCell = _board.getBoardCell(cell.column, cell.row); 
                        boardCell.token.blink(10);
                        boardCell.select(cell.colorCode);
                    }
                } 
            }

            _sndChannel = sound.play();
            _isEnded = true;
            
        }
        
        /*
         * Game service result handler. Depending on the type of call 
         * different actions are taken.
         */
        private function gameServiceResultHandler(event:ResultEvent):void{
            var call:Object = event.token;
            switch (call.operation){
                case GAME_SERVICE_GET_GRID_OP:
                    initGrid(GameGrid(event.result));
                    break;
                case GAME_SERVICE_GET_PLAYERS_OP:
                    updatePlayers (GenteGame (event.result));
                    break;
                case GAME_SERVICE_GET_MOVES_OP:
                    _moves = ArrayCollection(event.result);
                    _moveViewer.initFromMoves(_moves);
                    _selectedMoveInd = _moves.length - 1;
                    break;
                case GAME_SERVICE_DO_MOVE_OP:
                    var move:Move = Move (event.result);
                    _executingMove = GenteMove(move);
                    break;
            }
        }
        
        /*
         * Handles faults from game service calls 
         */
        private function gameServiceFaultHandler(event:FaultEvent):void{
            var errorMessage:ErrorMessage = ErrorMessage(event.message);
            if (errorMessage.extendedData != null){
                if(errorMessage.extendedData[ExceptionType.EXCEPTION_TYPE_KEY] == ExceptionType.INVALID_MOVE){
                    var fnc:Function = function (event:CloseEvent):void{
                        undoMove(_executingMove);
                    }
                    Alert.show(resourceManager.getString(
                            "StringsBundle", "gente.move.invalid"),
                            resourceManager.getString(
                                    "StringsBundle", "gente.move.error.title"), Alert.OK, null, fnc);
                }
            }else{
                Alert.show(event.fault.faultString, "Server Error");
            }
        }
        
        /*
         * Process the different types of messages received by the MessageReciever,
         * reordered and dispatched. All messages contain a game event
         * header, based on this different actions are taken.
         */
        private function processMessage(event:DynamicEvent):void {
            var message:IMessage = event.message;
            var gameId:Number = message.headers.GAME_ID;
            var gameEvent:String = message.headers.GAME_EVENT;
            var game:GenteGame = null;
        
            switch (gameEvent){
                case GameEvent.BEGIN:
                    begin();
                    break;
                case GameEvent.CHAT:
                    var chatMessage:ChatMessage = ChatMessage(message.body); 
                    _chatPanel.addMessage(chatMessage);
                    if(chatMessage.sender.id != _currentPlayer.registrant.id){
                        _gameStatus.showMessage(chatMessage.sender.registrant.name + " " +
                            resourceManager.getString(
                                    "StringsBundle", "gente.panel.chat.announcement"), 0x000000);
                    }
                    break;
                case GameEvent.END:
                    game = GenteGame (message.body);
                    _winners = game.winners;                
                    if (_isTurn){
                        end();
                    }
                    break;
                case GameEvent.MOVE_COMPLETE:
                    var move:GenteMove = GenteMove(message.body);
                    _executingMove = move;
                    addMove(move);
                    break;
                case GameEvent.PLAYER_CHANGE:
                    game = GenteGame (message.body);
                    _game = game;
                    updatePlayers(_game);
                    break;            
            }
        }
        
        /* Go directly to a given move in the move history of the game.
         * Animates tokens on or off the board to transform the current
         * board into a snapshot of the desired move.
         */
        private function gotoMove(event:DynamicEvent):void{

            var move:GenteMove = GenteMove(event.move);
            
            // if move is before the currently selected move then iterate
            // back over the moves transforming the board
            // else iterate forward
            if(move.id < GenteMove(_moves[_selectedMoveInd]).id){
                do{
                    undoMove(GenteMove(_moves[_selectedMoveInd]));
                    _selectedMoveInd --;                    
                }while(move.id < GenteMove(_moves[_selectedMoveInd]).id && _selectedMoveInd > 0);
            }else if (move.id > GenteMove(_moves[_selectedMoveInd]).id && _selectedMoveInd < _moves.length){
                do{
                    doMove(GenteMove(_moves[_selectedMoveInd + 1]));
                    _selectedMoveInd ++;
                }while(move.id > GenteMove(_moves[_selectedMoveInd]).id && _selectedMoveInd < _moves.length);
            }
        }
        
        /*
         * Adds a move to the internal list of moves. If the move is not present on the board then
         * it is animated.  
         */
        private function addMove(move:GenteMove):void{
            
            //get last move in game
            var lastMove:GenteMove = null;
            if (_moves.length > 0){
                lastMove = GenteMove(_moves[length - 1]);
            }
            
            //if move is after the last move then add moves
            //else update the move since its info may have changed
            if (lastMove == null || move.id > lastMove.id){
                
                //add to moves
                _moves.source.push(move);
                
                //if current move is the last move then animate
                if (_selectedMoveInd == _moves.length - 2){
                    _selectedMoveInd ++;
                    doMove(move);
                }
            }else{
                
                // Search for move in reverse order because its most likely to be the last move
                var oldMove:GenteMove;
                for (var i:Number = _moves.length - 1; i >= 0; i--){
                    oldMove = GenteMove(_moves[i]);
                    if (oldMove.id == move.id){
                        _moves[i] = move;
                        _moveViewer.updateMove(move);
                        break;
                    }
                }
            }
        }
        
        /*
         * Animates a move
         */
        private function doMove(move:GenteMove):void{
            
            //check that destination is free
            var boardCell:BoardCell = _board.getBoardCell(move.destinationCell.column, move.destinationCell.row);
            if (boardCell.token != null){
                _moveViewer.addMove(move);
                _moveViewer.selectedMove = move;
                if (_winners){
                    end();
                }else{
                    checkBeadStrings(move);
                }
                return;
            }
            
            //define origin
            var startPoint:Point;
            var startSize:Number;
            if(move.player.id == _currentPlayer.id && _isTurn){
                startPoint = new Point(_tokenStore.width, _tokenStore.height);
                startPoint = _tokenStore.localToGlobal(startPoint);
                startPoint = _animateLayer.globalToLocal(startPoint);
                startSize = _board.tokenSize;
            }else{
                var playerBtn:Button = _playersViewer.getPlayerButton(GentePlayer(move.player));
                startPoint = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y + Color.getCellIconSize() / 2 + 5);
                startPoint = _playersViewer.localToGlobal(startPoint);
                startPoint = _animateLayer.globalToLocal(startPoint);
                startSize = Color.getCellIconSize();
            }
            
            //define destination
            var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
            var endSize:Number = _board.tokenSize;
            endPoint = boardCell.localToGlobal(endPoint);
            endPoint = _animateLayer.globalToLocal(endPoint);
            
            //create new token
            var token:Token = new Token();
            token.cell = move.destinationCell;
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
        
        public function quitGame (gamePlayer:GamePlayer):void {
            var call:Object = _gameService.quitGame(_game, gamePlayer);
            call.operation = "quitGame";
            _isEnded = true;
        }        
        
        private function endDoMove(event:EffectEvent):void{
            
            var token:Token = Token(AnimateProperty(event.currentTarget).target);
            var boardCell:BoardCell = _board.getBoardCell(token.cell.column, token.cell.row);
            _animateLayer.removeChild(token);
            
            //remove from token store if necessary
            if(token.cell.color == _currentPlayer.color && _isTurn){
                _tokenStore.removeToken();
            }
            
            boardCell.token = token;
            boardCell.token.blink(1);
            boardCell.token.play();
            
            // Update move viewer
            var move:GenteMove = GenteMove(_moves[_selectedMoveInd])
            _moveViewer.addMove(move);
            _moveViewer.selectedMove = move;
            
            // if winners are not present or the move must be qualified
            if (!_winners || (move.player.id == getTeamMate().id)) {
                checkBeadStrings(move);

                if (move == _executingMove){
                    if (move.player.id == getTeamMate().id){
                        qualifyMove(move);
                    }
                }
            } else{
                end();
            }            
        }
        
        private function undoMove(move:GenteMove):void{
            
            //define origin
            var boardCell:BoardCell = _board.getBoardCell(move.destinationCell.column, move.destinationCell.row);
            var startPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
            var startSize:Number = _board.tokenSize;
            startPoint = boardCell.localToGlobal(startPoint);
            startPoint = _animateLayer.globalToLocal(startPoint);
            
            //define destination
            var endPoint:Point;
            var endSize:Number;
            if(move.player.id == _currentPlayer.id && _isTurn){
                endPoint = new Point(_tokenStore.width / 2, _tokenStore.height / 2);
                endPoint = _tokenStore.localToGlobal(endPoint);
                endPoint = _animateLayer.globalToLocal(endPoint);
                endSize = _board.tokenSize;
            }else{
                var playerBtn:Button = _playersViewer.getPlayerButton(GentePlayer(move.player));
                endPoint = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y + Color.getCellIconSize() / 2 + 5);
                endPoint = _playersViewer.localToGlobal(endPoint);
                endPoint = _animateLayer.globalToLocal(endPoint);
                endSize = Color.getCellIconSize();
            }
            
            //create new token
            var token:Token = new Token();
            token.cell = move.destinationCell;
            token.width = endSize;
            token.height = endSize;
            boardCell.token = null;
            boardCell.reset();
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
            _animateLayer.removeChild(token);
            
            //add to token store if necessary
            if(token.cell.color == _currentPlayer.color && _isTurn){
                _tokenStore.addToken();
            }
            _moveViewer.selectedMove = GenteMove(_moves[_selectedMoveInd]);
        }
        
        private function checkBeadStrings(move:GenteMove):void{

            // ifthe move contains trias or tesseras then blink them
            var beadString:BeadString;
            var cell:Cell;
            var hasScored:Boolean = false;
            if (move.trias != null && move.trias.length){
                hasScored = true;
                for (var i:Number = 0; i < move.trias.length; i++){
                    beadString = BeadString(move.trias[i]);
                    for (var j:Number = 0; j < beadString.beads.length; j++){
                        cell = Cell(beadString.beads[j]);
                        _board.getBoardCell(cell.column, cell.row).token.blink(3);
                    }
                }
            }
            if (move.tesseras != null && move.tesseras.length){
                hasScored = true;
                for (var k:Number = 0; k < move.tesseras.length; k++){
                    beadString = BeadString(move.tesseras[k]);
                    for (var l:Number = 0; l < beadString.beads.length; l++){
                        cell = Cell(beadString.beads[l]);
                        _board.getBoardCell(cell.column, cell.row).token.blink(3);
                    }
                }
            }
            
            if(hasScored){
                _gameStatus.showMessage(move.player.registrant.name + "  " +
                    resourceManager.getString("StringsBundle", "gente.move.scored"), Color.getColorCode(move.player.color));
            }

        }
        
        private function qualifyMove(move:GenteMove):void{
            var fnc:Function = function(eventObj:CloseEvent):void{
                
                switch (eventObj.detail){
                    case Alert.YES:
                        move.qualifier = CooperatiionQualifier.COOPERATIVE;
                        break;
                    case Alert.NO:
                        move.qualifier = CooperatiionQualifier.SELFISH;
                        break;
                    case Alert.CANCEL:
                        move.qualifier = CooperatiionQualifier.NEUTRAL;
                        break;
                }
                var call:Object = _gameService.updateMove(move);
                call.operation = GAME_SERVICE_UPDATE_MOVE_OP;
                
                // if no winners then reset board cell and continue with the game
                // else do the winning routine
                if (!_winners){
                    var boardCell:BoardCell = _board.getBoardCell(move.destinationCell.column, move.destinationCell.row); 
                    boardCell.reset();
                } else {
                    end();
                }
            }
            
            // select board cell and start blinking token
            var boardCell:BoardCell = _board.getBoardCell(move.destinationCell.column, move.destinationCell.row);
            boardCell.select(Color.getColorCode(move.player.color));
            
            // show qualify dialog
            var txt:String = resourceManager.getString("StringsBundle", "move.qualify.announcement") + "\n\n" +
                    resourceManager.getString("StringsBundle", "move.qualify.question");
            var title:String = resourceManager.getString("StringsBundle", "move.qualify.title");

            Alert.yesLabel = resourceManager.getString("StringsBundle", "move.qualify.button.cooperate");
            Alert.noLabel = resourceManager.getString("StringsBundle", "move.qualify.button.selfish");
            Alert.cancelLabel = resourceManager.getString("StringsBundle", "move.qualify.button.cancel");
            Alert.buttonWidth = 120;
            Alert.show(txt, title, Alert.YES | Alert.NO | Alert.CANCEL, _board, fnc, null, Alert.CANCEL);
        }
        
        private function getGrid():void{
            var call:Object = _gameService.getGameGrid(_gameId);
            call.operation = "getGameGrid";
        }
        
        private function initGrid(gameGrid:GameGrid):void{
            var cell:Cell;
            var token:Token;
            _gameGrid = gameGrid;
            _board.clearTokens();
            if (_gameGrid != null && _gameGrid.cells && _gameGrid.cells.length > 0){
                for (var i:Number = 0; i < _gameGrid.cells.length; i++){
                    cell = Cell(_gameGrid.cells[i]);
                    token = new Token();
                    token.cell = cell;
                    _board.addToken(token);
                }
            }
        } 
        
        private function initTurn():void{
            _tokenStore.active = true;
            _currentPlayer.play();
        }
        
        private function endTurn():void{
            _tokenStore.active = false;
        }
        
        private function startMove(evt:MouseEvent):void{
            
            if (!_isMoving && _isTurn){
            
                // initialize drag source
                var token:Token = Token(evt.currentTarget);
                var ds:DragSource = new DragSource();
                ds.addData(token, "token");
                                
                // create proxy image and start drag
                var dragImage:IFlexDisplayObject = token.createDragImage();
                DragManager.doDrag(token, ds, evt, dragImage);
                _isMoving = true;
                
            }
        } 
        
        private function dragEnterBoardCell(evt:DragEvent):void{
            
            if (evt.dragSource.hasFormat("token")){
                
                var token:Token = Token(evt.dragSource.dataForFormat("token"));
                var boardCell:BoardCell = BoardCell(evt.currentTarget);
                boardCell.addEventListener(DragEvent.DRAG_EXIT, dragExitCell);
                
                // calculate if move is valid
                if (validateMove(boardCell)){
                    boardCell.select(token.cell.colorCode);
                    DragManager.acceptDragDrop(boardCell);
                }
            }
        }  
        
        private function dragDropCell(evt:DragEvent):void{
            
            if (evt.dragSource.hasFormat("token")){
                
                // define cell and destination
                var token:Token = Token (evt.dragSource.dataForFormat("token"));
                var boardCell:BoardCell = BoardCell(evt.currentTarget);
                var destination:Cell = token.cell.clone();
                destination.row = boardCell.row;
                destination.column = boardCell.column;
                
                // do move in backend
                var move:GenteMove = new GenteMove();
                move.player = _currentPlayer;
                move.destinationCell = destination;
                move.status = String (MoveStatus.UNVERIFIED);
                var call:Object = _gameService.doMove(_game, move);
                call.operation = GAME_SERVICE_DO_MOVE_OP;
                _executingMove = move; 
                
                // do move in interface
                boardCell.reset();
                _tokenStore.removeToken();
                var newToken:Token = new Token();
                newToken.cell = destination;
                _board.addToken(newToken);
            }
        }
        
        private function dragExitCell(evt:DragEvent):void{
            
            // unselect board cell
            if (evt.dragSource.hasFormat("token")){
                var boardCell:BoardCell = BoardCell(evt.currentTarget);
                boardCell.reset();
            }
        } 
        
        private function endMove(evt:DragEvent):void{
            
            if (evt.dragSource.hasFormat("token")){
                _isMoving = false;
                var token:Token = Token(evt.currentTarget);
                token.selected = false;
                token.play();
            }
        }
        
        private function highlightToken(evt:MouseEvent):void{
            
            if (!_isMoving && _isTurn){
                var token:Token = Token(evt.currentTarget);
                token.useHandCursor = true;
                token.selected = true;;
            }
        }
        
        private function restoreToken(evt:MouseEvent):void{
            
            if (!_isMoving && _isTurn){
                var token:Token = Token(evt.currentTarget);
                token.useHandCursor = false;
                token.selected = false;
            }
        }
        
        private function validateMove(boardCell:BoardCell):Boolean{
            
            if(_moves.length == 0){
                if(boardCell.row == Math.floor(_board.nRows / 2) && boardCell.column == Math.floor(_board.nCols / 2)){
                    return true;
                }else{
                    return false;
                }
            }else if (boardCell.token == null){
                return true;
            }
            return false;   
        }
        
        private function getTeamMate():GamePlayer{
            
            // get team mates color
            var color:String;
            switch (_currentPlayer.color){
                case Color.YELLOW:
                    color = Color.RED;
                    break;
                case Color.BLUE:
                    color = Color.GREEN;
                    break;
                case Color.RED:
                    color = Color.YELLOW;
                    break;
                case Color.GREEN:
                    color = Color.BLUE;
                    break;
            }
            
            // get team mate
            var player:GamePlayer;
            for (var i:int = 0; i < _players.length; i++){
                player = GamePlayer(_players[i])
                if (player.color == color){
                    return player;
                }
            }
            return null;
        }
        
        public function destroy():void{
            _msgReceiver.destroy();
        }
    }
}
