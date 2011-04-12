//copyright

package mx.ecosur.multigame.manantiales {

    import flash.geom.Point;
    
    import mx.controls.Button;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.entity.manantiales.Ficha;
    import mx.ecosur.multigame.entity.manantiales.ManantialesMove;
    import mx.ecosur.multigame.entity.manantiales.ManantialesPlayer;
    import mx.ecosur.multigame.enum.manantiales.TokenType;
    import mx.ecosur.multigame.manantiales.token.ForestToken;
    import mx.ecosur.multigame.manantiales.token.IntensiveToken;
    import mx.ecosur.multigame.manantiales.token.ManantialesToken;
    import mx.ecosur.multigame.manantiales.token.ModerateToken;
    import mx.ecosur.multigame.manantiales.token.SilvopastoralToken;
    import mx.ecosur.multigame.manantiales.token.UndevelopedToken;
    import mx.ecosur.multigame.manantiales.token.ViveroToken;
    import mx.effects.AnimateProperty;
    import mx.events.DragEvent;
    import mx.events.DynamicEvent;
    import mx.events.EffectEvent;
    import mx.managers.DragManager;

    public class MoveHandler {

        /* Simple refacoring (Method to Class), will modify later
        * @TODO: Fix simple refactoring.
        * */
        private var _controller:ManantialesGameController;

        public function MoveHandler (controller:ManantialesGameController) {
            _controller = controller;
        }

        public function dragEnterBoardCell(evt:DragEvent):void{

         if (evt.dragSource.hasFormat("token")){
             var token:ManantialesToken = ManantialesToken(evt.dragSource.dataForFormat("token"));
             var boardCell:RoundCell = RoundCell(evt.currentTarget);
             boardCell.addEventListener(DragEvent.DRAG_EXIT, _controller._tokenHandler.dragExitCell);
             _controller._previousToken = ManantialesToken (boardCell.token);

             // calculate if move is valid
             if (validateMove(boardCell, token)){
                 boardCell.select(token.cell.colorCode);
                 DragManager.acceptDragDrop(boardCell);
             }
         }
        }

        private function validateMove(boardCell:RoundCell, token:ManantialesToken):Boolean {
         var ret:Boolean = false;
         if (boardCell.color == token.cell.color) {
                 ret = true;
         } else if (boardCell.color == Color.UNKNOWN) {
             switch (token.cell.color) {
                 case Color.YELLOW:
                     ret = (boardCell.column < 5 && boardCell.row < 5);
                     break;
                 case Color.PURPLE:
                     ret = (boardCell.column < 5 && boardCell.row > 3);
                     break;
                 case Color.RED:
                     ret = (boardCell.column > 3 && boardCell.row < 5);
                     break;
                 case Color.BLACK:
                     ret = (boardCell.column > 3 && boardCell.row > 3);
                     break;
            }
         }

         return ret;
        }


        /* Go directly to a given move in the move history of the game.
        * Animates tokens on or off the board to transform the current
        * board into a snapshot of the desired move.
        */
        public function gotoMove(event:DynamicEvent):void{

         var move:ManantialesMove = ManantialesMove(event.move);

         // if move is before the currently selected move then iterate
         // back over the moves transforming the board
         // else iterate forward
         if(move.id < ManantialesMove(_controller._moves[_controller._selectedMoveInd]).id){
             do{
                 undoMove(ManantialesMove(_controller._moves[_controller._selectedMoveInd]));
                 _controller._selectedMoveInd --;
             }while(move.id < ManantialesMove(_controller._moves[_controller._selectedMoveInd]).id
                     && _controller._selectedMoveInd > 0);
         }else if (move.id > ManantialesMove(_controller._moves[_controller._selectedMoveInd]).id
                     && _controller._selectedMoveInd < _controller._moves.length){
             do{
                 doMove(ManantialesMove(_controller._moves[_controller._selectedMoveInd + 1]));
                 _controller._selectedMoveInd ++;
             } while (move.id > ManantialesMove(_controller._moves[_controller._selectedMoveInd]).id
                 && _controller._selectedMoveInd < _controller._moves.length);
         }
        }

        public function addMove(move:ManantialesMove):void {
            if (move.mode == _controller._game.mode && !_controller._isTurn)
                _controller._gameWindow.currentState = "";

            if (move.mode == _controller._game.mode) {

                //get last move in game
                var lastMove:ManantialesMove = null;
                if (_controller._moves.length > 0){
                    lastMove = ManantialesMove(_controller._moves[length - 1]);
                }

                if (move.badYear) {
                    
                    _controller._moves.source.push(move);
                    _controller._gameWindow.moveViewer.addMove(move);

                } else if (lastMove == null || move.id > lastMove.id){

                    /* Suggestions are cleared on completion of any turn based moves */
                    if (move.player.turn)
                        _controller._suggestionHandler.clearSuggestions();

                    //add to moves
                    _controller._moves.source.push(move);
                    _controller._gameWindow.moveViewer.addMove(move);

                    //if current move is the last move then animate
                    if (_controller._selectedMoveInd == _controller._moves.length - 2){
                        _controller._selectedMoveInd ++;
                        doMove(move);
                    }
                } else {
                    // Search for move in reverse order because its most likely to be the last move
                    var oldMove:ManantialesMove;
                    for (var i:Number = _controller._moves.length - 1; i >= 0; i--){
                        oldMove = ManantialesMove(_controller._moves[i]);
                        if (oldMove.id == move.id){
                            _controller._moves[i] = move;
                            _controller._gameWindow.moveViewer.updateMove(move);
                            break;
                        }
                    }
                }

                /* If PUZZLE, check and see if the move was suggested, and if so,
                remove the token connected to the move's "currentCell" -- thereby
                completing the suggested move */
                if (_controller.puzzleMode && move.currentCell != null) {
                    var cell:RoundCell = RoundCell(_controller._gameWindow.board.getBoardCell(
                        move.currentCell.column, move.currentCell.row));                    
                    cell.token = new UndevelopedToken();
                    cell.reset();
                }
            }
        }

        /*
         * Animates a move
         */
        private function doMove(move:ManantialesMove):void{
        	
            // if was a bad year, then nothing to do
            if(move.badYear){
                _controller._gameWindow.moveViewer.selectedMove = ManantialesMove(_controller._moves[_controller._selectedMoveInd + 1]);
                return
            }

            var boardCell:RoundCell;
            var token:ManantialesToken;

            /* Check that destination is free */
            if (move.destinationCell != null) {
               boardCell = RoundCell(_controller._gameWindow.board.getBoardCell(
                    move.destinationCell.column, move.destinationCell.row));
                if (!boardCell.token is UndevelopedToken || boardCell.token is IntensiveToken)
                {
                    _controller._gameWindow.moveViewer.selectedMove = move;
                    return;
                }
            } else {
                boardCell = RoundCell(_controller._gameWindow.board.getBoardCell(
                    move.currentCell.column, move.currentCell.row));
                token = new UndevelopedToken();
                token.cell = move.currentCell;
                boardCell.reset();
            }

            var current:Ficha;
            var currentCell:RoundCell;

            if (move.currentCell != null && move.currentCell is Ficha){
                current = Ficha (move.currentCell);
                currentCell = RoundCell(_controller._gameWindow.board.getBoardCell(current.column, current.row));
            }

            var destination:Ficha
            if (move.destinationCell != null)
                destination = Ficha(move.destinationCell);

            //define origin
            var startPoint:Point;
            var startSize:Number;
            var playerBtn:Button;

            if(move.player.id == _controller._currentPlayer.id && _controller._isTurn){
                if (currentCell == null) {
                    switch (destination.type) {
                        case TokenType.FOREST:
                           startPoint = new Point(_controller._gameWindow.forestStore.width, _controller._gameWindow.forestStore.height);
                           startPoint = _controller._gameWindow.forestStore.localToGlobal(startPoint);
                           break;
                        case TokenType.INTENSIVE:
                            startPoint = new Point(_controller._gameWindow.intensiveStore.width, _controller._gameWindow.intensiveStore.height);
                            startPoint = _controller._gameWindow.intensiveStore.localToGlobal(startPoint);
                            break;
                        case TokenType.MODERATE:
                            startPoint = new Point(_controller._gameWindow.moderateStore.width, _controller._gameWindow.moderateStore.height);
                            startPoint = _controller._gameWindow.moderateStore.localToGlobal(startPoint);
                            break;
                        case TokenType.SILVOPASTORAL:
                            startPoint = new Point(_controller._gameWindow.silvoStore.width, _controller._gameWindow.silvoStore.height);
                            startPoint = _controller._gameWindow.silvoStore.localToGlobal(startPoint);
                            break;
                        case TokenType.VIVERO:
                            startPoint = new Point(_controller._gameWindow.viveroStore.width, _controller._gameWindow.viveroStore.height);
                            startPoint = _controller._gameWindow.viveroStore.localToGlobal(startPoint);
                            break;
                       default:
                            break;
                    }
                } else {
                    startPoint = new Point(currentCell.width, currentCell.height);
                    startPoint = currentCell.localToGlobal(startPoint);
                }

                startPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);
                startSize = _controller._gameWindow.board.tokenSize;

            } else if (currentCell != null) {
                startPoint = new Point(currentCell.width / 2, currentCell.height / 2);
                startSize = _controller._gameWindow.board.tokenSize;
                startPoint = currentCell.localToGlobal(startPoint);
                startPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);
            } else {
                playerBtn = _controller._gameWindow.playersViewer.getPlayerButton(
                    ManantialesPlayer(move.player));
                startPoint = new Point(
                    playerBtn.x + Color.getCellIconSize() / 2 + 5,
                    playerBtn.y + Color.getCellIconSize() / 2 + 5);
                startPoint = _controller._gameWindow.playersViewer.localToGlobal(startPoint);
                startPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);
                startSize = Color.getCellIconSize();
            }

            var endPoint:Point;
            var endSize:Number;

            //define destination
            if (destination != null) {
                endPoint = new Point(boardCell.width / 2, boardCell.height / 2);
                endSize = _controller._gameWindow.board.tokenSize;
                endPoint = boardCell.localToGlobal(endPoint);
                endPoint = _controller._gameWindow.animateLayer.globalToLocal(endPoint);
            } else if (move.currentCell != null) {
                playerBtn = _controller._gameWindow.playersViewer.getPlayerButton(
                    ManantialesPlayer(move.player));
                endPoint = new Point(
                    playerBtn.x + Color.getCellIconSize() / 2 + 5,
                    playerBtn.y + Color.getCellIconSize() / 2 + 5);
                endPoint = _controller._gameWindow.playersViewer.localToGlobal(startPoint);
                endPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);
                endSize = Color.getCellIconSize();
            }
            
            var isSuggestion:Boolean = _controller.puzzleMode && move.currentCell != null;
            if (move.player.id != _controller._currentPlayer.id || isSuggestion) {
                var existing:ManantialesToken;

                if (destination != null) {
                    switch (destination.type) {
                        case TokenType.FOREST:
                           token = new ForestToken();
                           break;
                        case TokenType.INTENSIVE:
                           token = new IntensiveToken();
                           break;
                        case TokenType.MODERATE:
                           token = new ModerateToken();
                           break;
                        case TokenType.SILVOPASTORAL:
                           token = new SilvopastoralToken();
                           break;
                        case TokenType.VIVERO:
                           token = new ViveroToken();
                           break;
                        default:
                           break;
                    }
                    token.cell = move.destinationCell;
                } 

                token.width = endSize;
                token.height = endSize;
                _controller._gameWindow.animateLayer.addChild(token);

                if(_controller.puzzleMode){
                    _controller._tokenHandler.addListeners(token);
                }

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
        }

        private function endDoMove(event:EffectEvent):void{

            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
            if (token.cell != null) {
                var boardCell:BoardCell = _controller._gameWindow.board.getBoardCell(token.cell.column, token.cell.row);
                _controller._gameWindow.animateLayer.removeChild(token);

                //remove from token store if necessary
                if(token.cell.color == _controller._currentPlayer.color && _controller._isTurn){
                    if (token is ForestToken) {
                        _controller._gameWindow.forestStore.removeToken();
                    }
                    else if (token is IntensiveToken) {
                        _controller._gameWindow.intensiveStore.removeToken();
                    }
                    else if (token is ModerateToken) {
                        _controller._gameWindow.moderateStore.removeToken();
                    }
                    else if (token is ViveroToken) {
                        _controller._gameWindow.viveroStore.removeToken();
                    }
                    else if (token is SilvopastoralToken) {
                        _controller._gameWindow.silvoStore.removeToken();
                    }
                }

                boardCell.token = token;
                boardCell.reset();
                boardCell.token.blink(1);
                boardCell.token.play();

                // Update move viewer
                if (_controller._selectedMoveInd > 0 && _controller._moves.length > _controller._selectedMoveInd && _controller._moves[_controller._selectedMoveInd] != null) {
                    var move:ManantialesMove = ManantialesMove(_controller._moves[_controller._selectedMoveInd])
                    _controller._gameWindow.moveViewer.selectedMove = move;
                }
            }
        }

        /* Function for handling invalid move processing */
        public function invalidMove(move:ManantialesMove):void {
           var boardCell:BoardCell = _controller._gameWindow.board.getBoardCell(
                   move.destinationCell.column, move.destinationCell.row);
            var ficha:Ficha;

            // if was a bad year then nothing to undo
            if(move.badYear){
                _controller._gameWindow.moveViewer.selectedMove = ManantialesMove(_controller._moves[_controller._selectedMoveInd - 1]);
                return
            }

            /* Restore previous token */
            if (move.currentCell != null) {
                ficha = Ficha (move.currentCell);
                switch (ficha.type) {
                    case TokenType.INTENSIVE:
                        boardCell.token = new IntensiveToken();
                        _controller._gameWindow.intensiveStore.removeToken();
                        break;
                    case TokenType.MODERATE:
                        boardCell.token = new ModerateToken();
                        _controller._gameWindow.moderateStore.removeToken();
                        break;
                    case TokenType.FOREST:
                        boardCell.token = new ForestToken();
                        _controller._gameWindow.forestStore.removeToken();
                        break;
                    case TokenType.VIVERO:
                        boardCell.token = new ViveroToken();
                        _controller._gameWindow.viveroStore.removeToken();
                        break;
                    case TokenType.SILVOPASTORAL:
                        boardCell.token = new SilvopastoralToken();
                        _controller._gameWindow.silvoStore.removeToken();    
                        break;
                    default:
                        break;
                }

                boardCell.token.cell = ficha;

            } else
                boardCell.token = new UndevelopedToken();

            /* Increment the INVALID move's token store */
            if (move.destinationCell != null) {
                ficha = Ficha(move.destinationCell);
                switch (ficha.type) {
                    case TokenType.INTENSIVE:
                        _controller._gameWindow.intensiveStore.addToken();
                        break;
                    case TokenType.MODERATE:
                        _controller._gameWindow.moderateStore.addToken();
                        break;
                    case TokenType.FOREST:
                        _controller._gameWindow.forestStore.addToken();
                        break;
                    case TokenType.VIVERO:
                        _controller._gameWindow.viveroStore.addToken();
                        break;
                    case TokenType.SILVOPASTORAL:
                        _controller._gameWindow.silvoStore.addToken();
                        break;
                    default:
                        break;
                }
            }

            /* An animation would be nice here */
            
            /* Reset the cell on the board */
            boardCell.reset();
        }

        protected function undoMove(move:ManantialesMove):void{
            var boardCell:BoardCell;
            var startPoint:Point;
            var startSize:Number

            var endCell:BoardCell;
            var endPoint:Point;
            var endSize:Number;
            var playerBtn:Button

            // if was a bad year then nothing to undo
            if(move.badYear){
                _controller._gameWindow.moveViewer.selectedMove = ManantialesMove(_controller._moves[_controller._selectedMoveInd - 1]);
                return
            }


            //define origin
            boardCell = _controller._gameWindow.board.getBoardCell(move.destinationCell.column,
                    move.destinationCell.row);
            startPoint = new Point(boardCell.width / 2, boardCell.height / 2);
            startSize = _controller._gameWindow.board.tokenSize;
            startPoint = boardCell.localToGlobal(startPoint);
            startPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);

            //define destination
                if (move.currentCell == null) {
                    playerBtn = _controller._gameWindow.playersViewer.getPlayerButton(ManantialesPlayer(move.player));
                    endPoint = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y +
                        Color.getCellIconSize() / 2 + 5);
                    endPoint = _controller._gameWindow.playersViewer.localToGlobal(endPoint);
                    endPoint = _controller._gameWindow.animateLayer.globalToLocal(endPoint);
                    endSize = Color.getCellIconSize();
                } else {

                    endCell = _controller._gameWindow.board.getBoardCell(move.currentCell.column, move.currentCell.row);
                    endPoint = new Point (endCell.width/ 2, endCell.height/2);
                    endSize = _controller._gameWindow.board.tokenSize;
                    endPoint = endCell.localToGlobal(endPoint);
                    endPoint = _controller._gameWindow.animateLayer.globalToLocal(endPoint);

                }

            // restore previous token, if determinable from move
            if (move.currentCell != null) {
                for (var index:int = 1; index <= _controller._moves.length; index++) {
                    var possible:ManantialesMove = _controller._moves [ _controller._moves.length - index];
                    if (possible.destinationCell == move.currentCell) {
                        var ficha:Ficha = Ficha (possible.destinationCell);
                        switch (ficha.type) {
                            case TokenType.INTENSIVE:
                                boardCell.token = new IntensiveToken();
                                break;
                            case TokenType.MODERATE:
                                boardCell.token = new ModerateToken();
                                break;
                            case TokenType.FOREST:
                                boardCell.token = new ForestToken();
                                break;
                            case TokenType.VIVERO:
                                boardCell.token = new ViveroToken();
                                break;
                            case TokenType.SILVOPASTORAL:
                                boardCell.token = new SilvopastoralToken();
                                break;
                            default:
                                boardCell.token = new UndevelopedToken();
                                break;

                        }

                        boardCell.token.cell = ficha;
                    }
                }
            } else
                boardCell.token = new UndevelopedToken();

            //define motion animation
            var apX:AnimateProperty = new AnimateProperty(boardCell.token);
            apX.fromValue = startPoint.x;
            apX.toValue = endPoint.x;
            apX.duration = 1000;
            apX.property = "x";
            var apY:AnimateProperty = new AnimateProperty(boardCell.token);
            apY.fromValue = startPoint.y;
            apY.toValue = endPoint.y;
            apY.duration = 1000;
            apY.property = "y";
            apY.addEventListener(EffectEvent.EFFECT_END, endUndoMove);

            //define size animation
            var apXScale:AnimateProperty = new AnimateProperty(boardCell.token);
            apXScale.property = "scaleX";
            apXScale.fromValue = startSize / endSize;
            apXScale.toValue = 1;
            apXScale.duration = 1000;
            var apYScale:AnimateProperty = new AnimateProperty(boardCell.token);
            apYScale.property = "scaleY";
            apYScale.fromValue = startSize / endSize;
            apYScale.toValue = 1;
            apYScale.duration = 1000;

            //start effect
            apX.play();
            apY.play();
            apXScale.play();
            apYScale.play();

            boardCell.reset();
        }

        private function endUndoMove(event:EffectEvent):void {
            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
            if (_controller._gameWindow.animateLayer.contains(token))
                _controller._gameWindow.animateLayer.removeChild(token);
            if (_controller._selectedMoveInd >= 0)
                _controller._gameWindow.moveViewer.selectedMove = ManantialesMove(_controller._moves[_controller._selectedMoveInd]);
        }
    }
}
