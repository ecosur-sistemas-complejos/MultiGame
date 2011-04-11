package mx.ecosur.multigame.manantiales {

import flash.events.MouseEvent;

    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.core.DragSource;
    import mx.core.IFlexDisplayObject;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.MoveStatus;
    import mx.ecosur.multigame.entity.manantiales.Ficha;
    import mx.ecosur.multigame.entity.manantiales.ManantialesMove;
    import mx.ecosur.multigame.entity.manantiales.ManantialesPlayer;
    import mx.ecosur.multigame.enum.manantiales.TokenType;
    import mx.ecosur.multigame.manantiales.token.ForestToken;
    import mx.ecosur.multigame.manantiales.token.ForestTokenStore;
    import mx.ecosur.multigame.manantiales.token.IntensiveToken;
    import mx.ecosur.multigame.manantiales.token.IntensiveTokenStore;
    import mx.ecosur.multigame.manantiales.token.ManantialesToken;
    import mx.ecosur.multigame.manantiales.token.ManantialesTokenStore;
    import mx.ecosur.multigame.manantiales.token.ModerateToken;
    import mx.ecosur.multigame.manantiales.token.ModerateTokenStore;
    import mx.ecosur.multigame.manantiales.token.SilvopastoralToken;
    import mx.ecosur.multigame.manantiales.token.SilvopastoralTokenStore;
    import mx.ecosur.multigame.manantiales.token.UndevelopedToken;
    import mx.ecosur.multigame.manantiales.token.ViveroToken;
    import mx.ecosur.multigame.manantiales.token.ViveroTokenStore;
    import mx.events.DragEvent;
    import mx.managers.DragManager;

    public class TokenHandler {

        private var _currentPlayer:ManantialesPlayer;

        private var _suggestionHandler:SuggestionHandler;

        private var _gameWindow:ManantialesWindow;

        private var _tokenStores:ArrayCollection;

        private var _mode:String;

        private var _isMoving:Boolean;

        public function TokenHandler(gameWindow:ManantialesWindow, player:ManantialesPlayer, handler:SuggestionHandler)
        {
            _currentPlayer = player;
            _suggestionHandler = handler;
            _gameWindow = gameWindow;
            _tokenStores = new ArrayCollection();
            _mode = gameWindow.currentState;
        }

        public function update (player:ManantialesPlayer):void {
            _currentPlayer = player;
        }

        public function processViolator (ficha:Ficha):void {
            if (ficha.color == _currentPlayer.color) {
                for (var i:int = 0; i < _tokenStores.length; i++) {
                    var store:ManantialesTokenStore = ManantialesTokenStore(_tokenStores.getItemAt(i));
                    if (store.tokenType == ficha.type) {
                        store.addToken();
                        break;
                    }
                }
            }
        }

        public function resetTokenStores():void {
            // initialize token stores
            for (var i:int = 0; i < _tokenStores.length; i++) {
                var store:ManantialesTokenStore = ManantialesTokenStore(_tokenStores.getItemAt(i));
                store.fill();

            }
        }

        public function initializeTokenStores ():void {
            // setup token store panels for hiding
            if (_tokenStores.getItemIndex(_gameWindow.forestStore) < 0)
                _tokenStores.addItem(_gameWindow.forestStore);
            if (_tokenStores.getItemIndex(_gameWindow.moderateStore) < 0)
                _tokenStores.addItem(_gameWindow.moderateStore);
            if (_tokenStores.getItemIndex(_gameWindow.intensiveStore) < 0)
                _tokenStores.addItem(_gameWindow.intensiveStore);

            if (_gameWindow.currentState == "SILVOPASTORAL" || _gameWindow.currentState == "SILVO_PUZZLE") {
                if (_tokenStores.getItemIndex(_gameWindow.viveroStore) < 0)
                    _tokenStores.addItem(_gameWindow.viveroStore);
                if (_tokenStores.getItemIndex(_gameWindow.silvoStore) < 0)
                    _tokenStores.addItem(_gameWindow.silvoStore);
            }

            // initialize token stores
            for (var i:int = 0; i < _tokenStores.length; i++) {
                var store:ManantialesTokenStore = ManantialesTokenStore(_tokenStores.getItemAt(i));
                if (store)
                    initializeTokenStore (store);
            }
        }

        private function initializeTokenStore (tokenStore:ManantialesTokenStore):void {
            tokenStore.controller = _gameWindow.controller;
            tokenStore.startMoveHandler = startMove;
            tokenStore.endMoveHandler = endMove;
            tokenStore.visible = true;
            tokenStore.active = true;
            tokenStore.init(_gameWindow.board.boardCells);
        }

        public function dragDropCell(evt:DragEvent):ManantialesMove {

            var suggestion:Boolean;
            var _executingMove:ManantialesMove;

            if (evt.dragSource.hasFormat("token"))
            {
                // define destination
                var destToken:ManantialesToken = ManantialesToken (evt.dragSource.dataForFormat("token"));
                var destination:Ficha = Ficha(destToken.ficha);

                var move:ManantialesMove = new ManantialesMove();
                move.status = String (MoveStatus.UNVERIFIED);
                move.mode = _mode;
                move.id = 0;

                // define target cell
                var targetCell:BoardCell = BoardCell(evt.currentTarget);
                var sourceToken:ManantialesToken = ManantialesToken (evt.dragSource.dataForFormat("source"));

                if (sourceToken.placed) {
                    if (sourceToken != null && sourceToken.cell != null) {
                        move.currentCell = sourceToken.cell;
                        var sourceCell:RoundCell = RoundCell (this._gameWindow.board.getBoardCell(
                                sourceToken.cell.column, sourceToken.cell.row));
                        sourceCell.token = new UndevelopedToken();
                        sourceCell.reset();
                    }
                }

                if (targetCell.token.cell != null) {
                    move.currentCell = Ficha (targetCell.token.cell);
                }

                /* Set the destination information to match where the token was dragged to */
                destination.row = targetCell.row;
                destination.column = targetCell.column;
                destination.type = destToken.ficha.type;
                move.destinationCell = destination;
                move.mode = _mode;

                if (_currentPlayer.turn && move.currentCell == null
                        && (destToken.cell.color == _currentPlayer.color || destToken.cell.color == Color.UNKNOWN))
                {
                        /* Regular Move */
                    move.player = _currentPlayer;
                    decrementStore(Ficha (move.destinationCell));

                     _gameWindow.controller.sendMove(move);

                } else if (_currentPlayer.turn &&
                        (destToken.cell.color == _currentPlayer.color || destToken.cell.color == Color.UNKNOWN) &&
                        move.currentCell != null)
                {
                    decrementStore(Ficha (move.destinationCell));
                    incrementStore(Ficha (move.currentCell));
                    move.player = _currentPlayer;
                    _gameWindow.controller.sendMove(move);

                }else if (_currentPlayer.turn &&
                        (destToken.cell.color != _currentPlayer.color || destToken.cell.color == Color.UNKNOWN))
                {
                        /* Making a suggestion to another player */
                    suggestion = true;                    
                    _suggestionHandler.makeSuggestion(move);
                } else {
                    Alert.show ("Unable to create move/suggestion.  destToken.cell=" + destToken.cell + ", " +
                            "destToken.cell.color==" + destToken.cell.color);
                }

                // animate
                targetCell.reset();

                var newToken:ManantialesToken;

                if (destToken is ForestToken) {
                    newToken = new ForestToken();
                }
                else if (destToken is IntensiveToken) {
                    newToken = new IntensiveToken();
                }
                else if (destToken is ModerateToken) {
                    newToken = new ModerateToken();
                }
                else if (destToken is ViveroToken) {
                    newToken = new ViveroToken();
                }
                else if (destToken is SilvopastoralToken) {
                    newToken = new SilvopastoralToken();
                }

                newToken.cell = destination;

                if (_gameWindow.controller.puzzleMode)
                    addListeners (newToken);

                _gameWindow.board.addToken(newToken);
            }

            return _executingMove;
        }

        private function decrementStore(ficha:Ficha):void {
            switch (ficha.type) {
                case TokenType.FOREST:
                    _gameWindow.forestStore.removeToken();
                    break;
                case TokenType.INTENSIVE:
                    _gameWindow.intensiveStore.removeToken();
                    break;
                case TokenType.MODERATE:
                    _gameWindow.moderateStore.removeToken();
                    break;
                case TokenType.VIVERO:
                    _gameWindow.viveroStore.removeToken();
                    break;
                case TokenType.SILVOPASTORAL:
                    _gameWindow.silvoStore.removeToken();
                    break;
                default:
                    break;
            }            
        }

        private function incrementStore (ficha:Ficha):void {
            switch (ficha.type) {
                case TokenType.FOREST:
                    _gameWindow.forestStore.addToken();
                    break;
                case TokenType.INTENSIVE:
                    _gameWindow.intensiveStore.addToken();
                    break;
                case TokenType.MODERATE:
                    _gameWindow.moderateStore.addToken();
                    break;
                case TokenType.VIVERO:
                    _gameWindow.viveroStore.addToken();
                    break;
                case TokenType.SILVOPASTORAL:
                    _gameWindow.silvoStore.addToken();
                    break;
                default:
                    break;
            }

        }

        public function dragExitCell(evt:DragEvent):void{

            // unselect board cell
            if (evt.dragSource.hasFormat("token")){
                var boardCell:BoardCell = BoardCell(evt.currentTarget);
                boardCell.reset();
            }
        }

        public function isTurn():Boolean {
            return _currentPlayer.turn;
        }

        public function startMove(evt:MouseEvent):void{

            if (!_isMoving && isTurn()){

                // initialize drag source
                var token:ManantialesToken = ManantialesToken(evt.currentTarget);
                var ds:DragSource = new DragSource();
                ds.addData(token, "token");

                // create proxy image and start drag
                var dragImage:IFlexDisplayObject = token.createDragImage();
                DragManager.doDrag(token, ds, evt, dragImage);
                _isMoving = true;

                var previous:ManantialesToken = ManantialesToken(evt.currentTarget);
                // Add previous to the drag source
                ds.addData(previous,"source");

            }
        }

        public function endMove(evt:DragEvent):void{

            if (evt.dragSource.hasFormat("token")){
                _isMoving = false;
                var token:ManantialesToken = ManantialesToken(evt.currentTarget);
                token.selected = false;
                token.play();

                // remove dragged image
                if (evt.dragSource is ManantialesToken) {
                    var previous:ManantialesToken = ManantialesToken (evt.dragSource);
                    if (previous != null && previous.ficha.column > 0 && previous.ficha.row > 0) {
                        var boardCell:BoardCell = _gameWindow.board.getBoardCell(previous.cell.column, previous.cell.row);
                        var undeveloped:UndevelopedToken = new UndevelopedToken();
                        undeveloped.cell = previous.cell;
                        boardCell.token = undeveloped;
                        boardCell.reset();
                    }
                }
            }
        }

        public function addListeners (token:ManantialesToken):void {
            if (token.cell) {
                if(token.cell.color == _currentPlayer.color){
                     token.addEventListener(MouseEvent.MOUSE_DOWN, startMove);
                     token.addEventListener(DragEvent.DRAG_COMPLETE, endMove);
                }else{
                    token.addEventListener(MouseEvent.MOUSE_DOWN, _suggestionHandler.startSuggestion);
                    token.addEventListener(DragEvent.DRAG_COMPLETE, _suggestionHandler.endSuggestion);
                    token.addEventListener(MouseEvent.CLICK, _suggestionHandler.typeSuggestion);
                    token.addEventListener(MouseEvent.DOUBLE_CLICK, _suggestionHandler.typeSuggestion);
                    token.doubleClickEnabled = true;
                }
            }
        }                               
    }
}