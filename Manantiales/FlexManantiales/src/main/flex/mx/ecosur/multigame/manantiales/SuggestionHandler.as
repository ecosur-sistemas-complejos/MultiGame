//copyright

package mx.ecosur.multigame.manantiales
{
    import flash.display.DisplayObject;
    import flash.events.MouseEvent;
    import flash.events.TimerEvent;
    import flash.geom.Point;
    import flash.utils.Dictionary;
    import flash.utils.Timer;

    import mx.collections.ArrayCollection;
    import mx.core.DragSource;
    import mx.core.IFlexDisplayObject;
    import mx.controls.Alert;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.enum.MoveStatus;
    import mx.ecosur.multigame.manantiales.entity.Ficha;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
    import mx.ecosur.multigame.manantiales.entity.Suggestion;
    import mx.ecosur.multigame.manantiales.enum.SuggestionStatus;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
    import mx.ecosur.multigame.manantiales.token.*;
    import mx.effects.AnimateProperty;
    import mx.events.DragEvent;
    import mx.events.DynamicEvent;
    import mx.events.EffectEvent;
    import mx.managers.DragManager;
    import mx.managers.PopUpManager;

    public class SuggestionHandler {

        private var _controller:ManantialesGameController;

        private var _player:ManantialesPlayer;

        private var _isMoving:Boolean;
        
        private var _animations:int;
        
        public var _mySuggestion:Suggestion;
        
        public var _currentSuggestions:Dictionary;
        
        public var _timer:Timer;

        private static const GAME_SERVICE_DO_SUGGESTION_OP:String = "makeSuggestion";
        
        private var _alert:SuggestionAlert;

        private var _tokenSuggestor:TokenSuggestor;

        public function SuggestionHandler (controller:ManantialesGameController) {
            _controller = controller;
            _player = ManantialesPlayer(controller._currentPlayer);
            _currentSuggestions = new Dictionary();
            _animations = 5;
        }
        
        private function animateSuggestion (suggestion:Suggestion):void {
            if (suggestion.move.player.color == _player.color) {
                var current:Ficha = Ficha (suggestion.move.currentCell);
                var currentCell:RoundCell = RoundCell(_controller._gameWindow.board.getBoardCell(
                        current.column, current.row));
                        
                /* Remove current */
                currentCell.token = new UndevelopedToken ();
                currentCell.token.cell = current;
                currentCell.reset();
                
                var destination:Ficha = Ficha (suggestion.move.destinationCell);
                var boardCell:RoundCell = RoundCell(_controller._gameWindow.board.getBoardCell(
                        suggestion.move.destinationCell.column, suggestion.move.destinationCell.row));
                        
                 //define origin
                var startPoint:Point;
                var startSize:Number;
    
                startPoint = new Point(currentCell.width /2, currentCell.height/2);
                startSize = boardCell.tokenSize;
                startPoint = currentCell.localToGlobal(startPoint);
                startPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);
    
                //define destination
                var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
                var endSize:Number = boardCell.tokenSize;
                endPoint = boardCell.localToGlobal(endPoint);
                endPoint = _controller._gameWindow.animateLayer.globalToLocal(endPoint);
    
                //create new token
                var token:ManantialesToken = new SuggestionToken(suggestion);
                
                token.cell = destination;
                token.width = endSize;
                token.height = endSize;
                _controller._gameWindow.animateLayer.addChild(token);
    
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
                
                token.blink();
            }
            
        }
        
        public function timerHandler (evt:TimerEvent):void {
            if (_mySuggestion != null) {
                animateSuggestion (_mySuggestion);
            }
            else {
                _timer.stop();
            }
        }

        public function addSuggestion (suggestion:Suggestion):void {
            _mySuggestion = suggestion;

            var move:ManantialesMove = suggestion.move;
            
            /* Check for an unintended suggestion */
            if (move.currentCell.column == move.destinationCell.column && move.currentCell.row == move.destinationCell.row)
                return;
            
            /* Animate suggestion */
            if (_currentSuggestions [ move.player.color ] == null)
                _currentSuggestions [move.player.color] = suggestion;

            animateSuggestion (suggestion);

            if (_timer != null && !_timer.running)
                _timer.start();            

            if (move.player.color == _player.color) {
                /* Pop up an Accept/Reject dialogue to determine what to do with suggestion */
                _alert = new SuggestionAlert();
                _alert.suggestion = suggestion;
                _alert.addEventListener("accept", accept);
                _alert.addEventListener("reject", reject);
                PopUpManager.addPopUp(_alert, _controller._gameWindow, true);
                    PopUpManager.centerPopUp(_alert);
            }
        }

        public function removeSuggestion (suggestion:Suggestion):void {
            var ficha:Ficha = Ficha (suggestion.move.destinationCell);
            
            //remove suggestion token
            for (var i:int = 0; i < _controller._gameWindow.animateLayer.numChildren; i++) {
                var obj:DisplayObject = _controller._gameWindow.animateLayer.getChildAt(i);
                if (obj is SuggestionToken) {
                    var suggestionToken:SuggestionToken = SuggestionToken (obj);
                    if (suggestionToken.cell.column == ficha.column && suggestionToken.cell.row == ficha.row) {
                        _controller._gameWindow.animateLayer.removeChild(obj);
                        break;
                    }
                }
            }
            
            /* if the suggestion was rejected, remove the "destination" piece from the board,
                retrieve the "current" piece from the move and add it to the board
             */
            if (suggestion.status == "REJECT") {
                var token:ManantialesToken = new ManantialesToken();
                ficha = Ficha (suggestion.move.currentCell);
                var destination:Ficha = Ficha (suggestion.move.destinationCell);
                switch (destination.type) {
                    case TokenType.INTENSIVE:
                            token = new IntensiveToken();
                            break;
                        case TokenType.MODERATE:
                            token = new ModerateToken();
                            break;
                        case TokenType.FOREST:
                            token = new ForestToken();
                            break;
                        case TokenType.VIVERO:
                            token = new ViveroToken();
                            break;
                        case TokenType.SILVOPASTORAL:
                            token = new SilvopastoralToken();
                            break;
                        default:
                            token = new UndevelopedToken();
                }                token.cell = ficha;
                
                if (token.className != "UndevelopedToken") {
                        token.addEventListener(MouseEvent.MOUSE_DOWN, startSuggestion);
                        token.addEventListener(DragEvent.DRAG_COMPLETE, makeSuggestion);
                }
                
                var boardCell:BoardCell = _controller._gameWindow.board.getBoardCell(
                        ficha.column, ficha.row);
                boardCell.token = null;
                boardCell.reset();
                boardCell.token = token;
                boardCell.reset();
                
                /* Now remove the destination (only in the case of the suggestor) */
                if (_controller._currentPlayer.color == suggestion.suggestor.color) {
                    boardCell =  _controller._gameWindow.board.getBoardCell(destination.column, destination.row);
                    token = new UndevelopedToken();
                    /* Reset */
                    boardCell.token = null;
                    boardCell.reset();
                    boardCell.token = token;
                    boardCell.reset();
                }
            }
            
            /* Always remove the suggestion from the dictionary */
            _currentSuggestions [suggestion.move.player.color] = null;
            if (suggestion == _mySuggestion)
                _mySuggestion = null;
        }
        
        public function endSuggestion(evt:DragEvent):void{
            // unselect cell
            if (evt.dragSource.hasFormat("token")){
                _isMoving = false;
                if (evt.currentTarget != null) {
                    var token:ManantialesToken = ManantialesToken(evt.currentTarget);
                    token.selected = false;
                } 
            }
            
            // remove dragged image
            if (evt.dragSource is ManantialesToken) {
                var previous:ManantialesToken = ManantialesToken (evt.dragSource);
                if (previous != null && previous.ficha.column > 0 && previous.ficha.row > 0) {
                    var boardCell:BoardCell = _controller._gameWindow.board.getBoardCell(previous.cell.column, previous.cell.row);
                    var undeveloped:UndevelopedToken = new UndevelopedToken();
                    undeveloped.cell = previous.cell;
                    boardCell.token = undeveloped;
                    boardCell.reset();
                }
            }
        }

        public function endRemoveSuggestion (event:EffectEvent):void {
            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
            var boardCell:BoardCell = _controller._gameWindow.board.getBoardCell(token.cell.column, token.cell.row);
            boardCell.token = token;
        }

        public function accept(event:DynamicEvent):void {
            PopUpManager.removePopUp(_alert);
            var suggestion:Suggestion = Suggestion (event.data);
            suggestion.status = SuggestionStatus.ACCEPT;
            send(suggestion);
        }

        public function reject(event:DynamicEvent):void {
            PopUpManager.removePopUp(_alert);
            var suggestion:Suggestion = Suggestion (event.data);
            suggestion.status = SuggestionStatus.REJECT;
            send (suggestion);
        }
        
        public function send (suggestion:Suggestion):void {
            if (suggestion != null) {
                var call:Object = _controller._gameService.makeSuggestion (this._controller._game, suggestion);
                call.operation = "makeSuggestion";
            }
            _mySuggestion = null;
            _alert = null;
        }
        
        public function clearSuggestions():void {
            if (_mySuggestion != null) {
                /* Only clears your suggestion (if there) and removes dialog */
                if (_alert != null)
                    PopUpManager.removePopUp(_alert);
                _mySuggestion.status = SuggestionStatus.REJECT;
                send (_mySuggestion);
                _mySuggestion = null;
            }
        }
        
        private function freePlayer (color:String):Boolean {
            return (_currentSuggestions[color] == null);
        }

        /* Drag/drop handlers for making suggestions on other player's boards */
        public function startSuggestion(evt:MouseEvent):void
        {
            var token:ManantialesToken = ManantialesToken(evt.currentTarget);
            
            if (freePlayer (token.cell.color) && _controller._currentPlayer.color == _player.color)
            {
                var ds:DragSource = new DragSource();
                ds.addData(token, "token");
                
                /* Set the previous token to Undeveloped */
                var previous:ManantialesToken = ManantialesToken(evt.currentTarget);

                // Add previous to the drag source 
                ds.addData(previous,"source");
                
                // create proxy image and start drag
                var dragImage:IFlexDisplayObject = token.createDragImage();
                DragManager.doDrag(token, ds, evt, dragImage);
            }
        }

        public function cancelSuggestion():void {
            PopUpManager.removePopUp(_tokenSuggestor);
        }

        public function suggestType():void {
            PopUpManager.removePopUp(_tokenSuggestor);
            var type:String = String (_tokenSuggestor.typeBox.selectedItem.data);
            var ficha:Ficha = null;

            if (type != _tokenSuggestor.source.ficha.type) {

               /* Basic clone of source ficha, modified to indicated type */
               ficha = new Ficha ();
               ficha.type = type;
               ficha.column = _tokenSuggestor.source.ficha.column;
               ficha.row = _tokenSuggestor.source.ficha.row;
               ficha.color = _tokenSuggestor.source.ficha.color;

                /* Make the suggestion */
               var suggestion:Suggestion = new Suggestion();
               var move:ManantialesMove = new ManantialesMove();
               move.player = _controller._currentPlayer;
               move.currentCell = _tokenSuggestor.source.ficha;
               move.destinationCell = ficha;
               move.mode = _controller._game.mode;
               suggestion.move = move;

                /* find the player the suggestion is targeted to */
               for (var i:int = 0; i < _controller._game.players.length; i++) {
                   var player:ManantialesPlayer = ManantialesPlayer (_controller._game.players.getItemAt(i));
                   if (player.color == _tokenSuggestor.source.ficha.color) {
                        suggestion.move.player = player;
                        break;
                   }
               }

               suggestion.suggestor = _player;
               suggestion.status = SuggestionStatus.UNEVALUATED;

               if (_currentSuggestions [move.player.color] == null)
                _currentSuggestions [move.player.color] = suggestion;

               var call:Object = new Object();
               call = _controller._gameService.makeSuggestion(_controller._game, suggestion);
               call.operation = GAME_SERVICE_DO_SUGGESTION_OP;
            }
        }

        public function typeSuggestion(evt:MouseEvent):void {
            var token:ManantialesToken = ManantialesToken(evt.currentTarget);

            if (freePlayer (token.cell.color) && _controller._currentPlayer.color == _player.color)
            {
                _tokenSuggestor = new TokenSuggestor();
                var _types:ArrayCollection = new ArrayCollection();
                var values:ArrayCollection = TokenType.values(_controller._game.mode);

                for (var i:int = 0; i < values.length; i++) {
                    var test:String = String (values.getItemAt(i));
                    if (test != token.type && test != TokenType.UNDEVELOPED)
                        _types.addItem(test);
                }

                _tokenSuggestor.types = _types;
                _tokenSuggestor.source = token;
                _tokenSuggestor.suggestor = suggestType;
                _tokenSuggestor.canceller = cancelSuggestion;

                PopUpManager.addPopUp(_tokenSuggestor, _controller._gameWindow, true);
                PopUpManager.centerPopUp(_tokenSuggestor);
            } else {
                Alert.show ("_controller._currentPlayer.turn=" + _controller._currentPlayer.turn +
                        ", _controller._currentPlayer.color == _player.color=" +
                        (_controller._currentPlayer.color == _player.color));
            }
        }

        public function makeSuggestion (move:ManantialesMove):void {
            if (move.player == null) {
                    var players:ArrayCollection = _controller._game.players;
                    for (var i:int = 0; i < players.length; i++) {
                        var player:ManantialesPlayer = ManantialesPlayer (players [ i ]);
                        if (player.color == move.destinationCell.color) {
                            move.player = player;
                            break;
                        }
                    }
            }

            if (move.currentCell != move.destinationCell) {
                move.status = String (MoveStatus.UNVERIFIED);
                move.mode = _controller._game.mode;
    
                var suggestion:Suggestion = new Suggestion();
                suggestion.move = move;
                suggestion.suggestor = _player;
                suggestion.status = SuggestionStatus.UNEVALUATED;
                
                if (_currentSuggestions [move.player.color] == null)
                    _currentSuggestions [move.player.color] = suggestion;
    
                var call:Object = new Object();
                call = _controller._gameService.makeSuggestion(_controller._game, suggestion);
                call.operation = GAME_SERVICE_DO_SUGGESTION_OP;
            }
        }
    }
}
