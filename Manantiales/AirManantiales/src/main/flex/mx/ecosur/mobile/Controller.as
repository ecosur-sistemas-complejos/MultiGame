/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 3/5/12
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.mobile {

import flash.events.Event;
import flash.events.MouseEvent;
import flash.events.TouchEvent;
import flash.geom.Point;
import flash.ui.Multitouch;
import flash.ui.MultitouchInputMode;

import mx.core.FlexGlobals;
import mx.ecosur.mobile.views.GameView;
import mx.ecosur.multigame.entity.GameGrid;
import mx.ecosur.multigame.enum.Color;
import mx.ecosur.multigame.enum.GameEvent;
import mx.ecosur.multigame.enum.GameState;
import mx.ecosur.multigame.enum.MoveStatus;
import mx.ecosur.multigame.manantiales.RoundCell;
import mx.ecosur.multigame.manantiales.entity.CheckCondition;
import mx.ecosur.multigame.manantiales.entity.Ficha;
import mx.ecosur.multigame.manantiales.entity.ManantialesGame;
import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
import mx.ecosur.multigame.manantiales.entity.Suggestion;
import mx.ecosur.multigame.manantiales.enum.ManantialesEvent;
import mx.ecosur.multigame.manantiales.enum.Mode;
import mx.ecosur.multigame.manantiales.enum.TokenType;
import mx.ecosur.multigame.manantiales.token.ForestToken;
import mx.ecosur.multigame.manantiales.token.IntensiveToken;
import mx.ecosur.multigame.manantiales.token.ManantialesToken;
import mx.ecosur.multigame.manantiales.token.ModerateToken;
import mx.ecosur.multigame.manantiales.token.SilvopastoralToken;
import mx.ecosur.multigame.manantiales.token.UndevelopedToken;
import mx.ecosur.multigame.manantiales.token.ViveroToken;

import mx.effects.Sequence;
import mx.messaging.events.MessageEvent;
import mx.messaging.events.MessageFaultEvent;
import mx.messaging.messages.IMessage;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

import spark.effects.Animate;
import spark.effects.easing.Bounce;

import spark.effects.Move;
import spark.effects.Fade;
import spark.effects.SetAction;
import spark.effects.animation.MotionPath;
import spark.effects.animation.SimpleMotionPath;
import spark.effects.easing.Sine;

public class Controller {

        private var _view:GameView;

        public function Controller(view:GameView) {
            super();
            this._view = view;
        }

        public function resultHandler(event:ResultEvent):void {
            if (event.result is ManantialesGame)
                updateGame (ManantialesGame(event.result));
        }
    
        public function faultHandler(event:FaultEvent):void {
            _view.alert(event.fault.message);
        }
    
        public function messageResultHandler(event:MessageEvent):void {
            var message:IMessage = event.message;
            var gameEvent:String = message.headers.GAME_EVENT;

            /* Holder objects */
            var game:ManantialesGame, checkCondition:CheckCondition, suggestion:Suggestion, move:ManantialesMove;
            
            switch (gameEvent) {
                case ManantialesEvent.BEGIN:
                    game = ManantialesGame(message.body);
                    updateGame(game);
                    updatePlayers(game);
                    break;
                case ManantialesEvent.CHAT:
                    // handle chat
                    break;
                case ManantialesEvent.END:
                    game = ManantialesGame(message.body);
                    end(game);
                    break;
                case ManantialesEvent.MOVE_COMPLETE:
                    move= ManantialesMove(message.body);
                    addMove(move);
                    break;
                case ManantialesEvent.PLAYER_CHANGE:
                    game = ManantialesGame(message.body);
                    updatePlayers(game);
                    break;
                case ManantialesEvent.CONDITION_RAISED:
                    checkCondition = CheckCondition(message.body);
                        _view.alert("CheckCondition Raised! [" + checkCondition.reason + "]");
//                    handleCheckConstraint (checkCondition);
                    break;
                case ManantialesEvent.CONDITION_RESOLVED:
                    checkCondition = CheckCondition(message.body);
                        _view.alert("CheckCondition relieved! [" + checkCondition.reason + "]");
//                    handleCheckConstraintResolved (checkCondition);
                    break;
                case ManantialesEvent.CONDITION_TRIGGERED:
                    checkCondition = CheckCondition(message.body);
                    _view.alert("Check condition triggered!")
//                    handleCheckConstraintTriggered (checkCondition);
                    break;
                case ManantialesEvent.SUGGESTION_EVALUATED:
                    suggestion = Suggestion(message.body);
//                    _suggestionHandler.addSuggestion (suggestion);
                    break;
                case ManantialesEvent.SUGGESTION_APPLIED:
                    suggestion = Suggestion(message.body);
//                    _suggestionHandler.removeSuggestion (suggestion);
                    break;
                case GameEvent.EXPIRED:
                    move = ManantialesMove(message.body);
                    _view.alert("Expired Move!");
                    end(_view.game);
//                    handleExpiredMove(move);
                    break;
                default:
                    break;
                }
            
        }
    
        public function messageFaultHandler(fault:MessageFaultEvent):void {
            _view.alert(fault.faultString);
        }

        public function updateGame(game:ManantialesGame):void {

            _view.game = game;

            for (var i:int = 0; i < _view.game.players.length; i++) {
                var p:ManantialesPlayer = ManantialesPlayer(_view.game.players [ i ]);
                if (p.name == FlexGlobals.topLevelApplication.registrant.name) {
                    _view.player = p;
                    break;
                }
            }

            _view.currentState = _view.game.mode;

            if (_view.game.state == GameState.PLAY) {
                var current:ManantialesPlayer = null;
                for (var i:int = 0; i < _view.game.players.length; i++) {
                    var p:ManantialesPlayer = ManantialesPlayer(_view.game.players [ i ]);
                    if (p.turn == true) {
                        current = p;
                        break;
                    }
                }

                if (p.name == FlexGlobals.topLevelApplication.registrant.name) {
                    _view.status.color = Color.getColorCode(p.color);
                    _view.status.showMessage("It's your turn");
                } else {
                    _view.status.color = Color.getColorCode(p.color);
                    _view.status.showMessage(p.name + " to move");
                }

            } else {
                _view.status.showMessage("Waiting for more players ...");
            }

            _view.timer.showMessage(_view.game.elapsedTime + " ms elapsed");
            populateBoard(_view.game);
            updatePlayers(_view.game);

        }
    
        public function populateBoard(game:ManantialesGame):void {
            var grid:GameGrid = game.grid;
            _view.board.clearTokens();
            Multitouch.inputMode = MultitouchInputMode.TOUCH_POINT;

            /* Setup undeveloped tokens first */
            for (var col:int = 0; col < _view.board.nCols; col++) {
                for (var row:int = 0; row < _view.board.nRows; row++) {
                    var roundCell:RoundCell = RoundCell(_view.board.getBoardCell(col, row));
                    if (roundCell != null) {
                        var tok:UndevelopedToken = new UndevelopedToken();
                        tok.col = col;
                        tok.row = row;
                        roundCell.token = tok;
                        tok.addEventListener(TouchEvent.TOUCH_TAP, tapHandler);
                        tok.addEventListener(MouseEvent.CLICK, tapHandler);
                    }
                }
            }

            var ficha:Ficha;
            var token:ManantialesToken;

            /* TODO: Move to store objects themselves. */
            if (grid && grid.cells && grid.cells.length > 0) {
                for (var i:int = 0; i < grid.cells.length; i++){
                    ficha = Ficha(grid.cells[i]);
                    var token:ManantialesToken = createToken(ficha.type);
                    token.ficha = ficha;
                    _view.board.addToken(token);
                    decorate(token);
                }
            }
        }
    
        public function end(game:ManantialesGame):void {
            if (game != null && game.state == GameState.ENDED) {
                _view.alert("Game over.");
            }
        }

        public function updatePlayers(game:ManantialesGame):void {
            _view.scorebox.game = game;
            _view.scorebox.updatePlayers();
           
            var p:ManantialesPlayer;
            
            for (var i:int = 0; i < game.players.length; i++) {
                var t:ManantialesPlayer = ManantialesPlayer(game.players [ i ]);
                if (t.turn) {
                    p = t;
                    break;
                }
            }
           
            /* If active player is not current registrant, message the status box */
            if (p.name != FlexGlobals.topLevelApplication.registrant.name) {
                _view.status.color = Color.getColorCode(p.color);
                _view.status.showMessage(p.name + "'s turn.");
            } else {
                _view.status.color = Color.getColorCode(p.color);
                _view.status.showMessage("Your turn.");
            }
        }

        public function tapHandler(event:Event):void {
            var dest:Point = new Point();
            var targ:ManantialesToken = ManantialesToken(event.currentTarget);
            var type:Object = _view.tokenTypes.getItemAt(_view.tokenType.selectedIndex);            
            
            var column:int,  row:int;
            if (targ is UndevelopedToken) {
                column = UndevelopedToken(targ).col;
                row = UndevelopedToken(targ).row;
            } else {
                column = targ.ficha.column;
                row = targ.ficha.row;
            }                               
                
            var rc:RoundCell = RoundCell(_view.board.getBoardCell(column,  row));
            dest.x = rc.x + (rc.width / 2); 
            dest.y = rc.y + (rc.height / 2);
            
            var tok:ManantialesToken = createToken(String(type.data));
            var ficha:Ficha = new Ficha();
            ficha.type = tok.type;
            ficha.row = rc.row;
            ficha.column = rc.column;
            ficha.color = _view.player.color;
            tok.ficha = ficha;

            /* Send the move over the wire */
            if (validateMove(rc,  tok)) {
                rc.select(tok.cell.colorCode);
                decorate(tok);
                animateMove(targ,  tok,  rc,  dest);
                var move:ManantialesMove = new ManantialesMove();
                move.status = MoveStatus.UNVERIFIED;
                move.mode = _view.game.mode;
                move.player = _view.player;
                move.currentCell = targ.ficha;
                move.destinationCell = tok.ficha;
                sendMove(move);
            }
        }

        /* Adds a move to the board that has arrived over the wire */
        public function addMove (move:ManantialesMove):void {
            if (move.player.name != FlexGlobals.topLevelApplication.registrant.name) {
                _view.status.showMessage(move.player.name + " has moved.");
                var target:Ficha = Ficha (move.destinationCell);

                /* NOTE: Need to add in handling of replacement moves */

                var rc:RoundCell = RoundCell(_view.board.getBoardCell(target.column,  target.row));
                var dest:Point = new Point();
                dest.x = rc.x + (rc.width /2);
                dest.y = rc.y + (rc.height / 2);

                var current:ManantialesToken = ManantialesToken (rc.token);
                var next:ManantialesToken = createToken(move.type);
                next.ficha = target;
                animateMove(current, next,  rc,  dest);
            }
        }

        protected function animateMove(start:ManantialesToken, end:ManantialesToken,  roundCell:RoundCell, dest:Point):void {
            
            /* Sin easer */
            var sine:Sine = new Sine();
            sine.easeInFraction = 0.7;

            /* Move effect */
            var drop:Move = new Move();
            drop.xFrom = start.x;
            drop.yFrom = start.y;
            drop.xTo = start.x;
            drop.yTo = _view.height - start.y;
            
            drop.target = start;
            drop.easer = sine;
            drop.duration = 350;

            /* Fade effect on existing token (after drop) */
            var fade:Fade = new Fade();
            fade.target = start;
            fade.alphaFrom = 1.0;
            fade.alphaTo = 0;
            fade.duration = 800;
           
            /* Set action */
            var sa:SetAction = new SetAction();
            sa.target = roundCell;
            sa.property="token";
            sa.value=end;

            /* Bounce effect on new token */
            var grw:Animate = new Animate();            
            grw.easer = new Bounce();
            grw.target = end;
            var inc:SimpleMotionPath = new SimpleMotionPath();
            inc.property = "scaleX";
            inc.valueFrom = 1.0;
            inc.valueTo = 1.5;
            var dec:SimpleMotionPath = new SimpleMotionPath();
            dec.property = "scaleX";
            dec.valueFrom = 1.5;
            dec.valueTo = 1.0;
            var vect:Vector.<MotionPath> = new Vector.<MotionPath>();
            vect.push(inc);
            vect.push(dec);
            grw.motionPaths = vect;

            var seq:Sequence = new Sequence();
            seq.addChild(drop);
            seq.addChild(fade);
            seq.addChild(sa);
            seq.addChild(grw);
            seq.play();
        }

        private function createToken(type:String):ManantialesToken {
            var token:ManantialesToken;

            switch (type) {
                case TokenType.FOREST:
                    token = new ForestToken();
                    break;
                case TokenType.MODERATE:
                    token = new ModerateToken();
                    break;
                case TokenType.INTENSIVE:
                    token = new IntensiveToken();
                    break;
                case TokenType.VIVERO:
                    token = new ViveroToken();
                    break;
                case TokenType.SILVOPASTORAL:
                    token = new SilvopastoralToken();
                    break;
                default:
                    break;
            }

            return token;

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

            ret = ret && _view.player.turn;
        
            return ret;
        }
    
        private function sendMove(move:ManantialesMove):void {
            _view.status.showMessage("Processing move ...");
            _view.gameService.doMove(_view.game, move);
        }

        private function decorate(tok:ManantialesToken):void {
            switch (_view.currentState) {
                case Mode.COMPETITIVE:
                    if (tok.type == TokenType.FOREST || tok.type == TokenType.MODERATE) {
                        tok.addEventListener(TouchEvent.TOUCH_TAP, tapHandler);
                        tok.addEventListener(MouseEvent.CLICK, tapHandler);
                    }
                    break;
                case Mode.SILVOPASTORAL:
                    if (tok.type == TokenType.SILVOPASTORAL) {
                        tok.addEventListener(TouchEvent.TOUCH_TAP, tapHandler);
                        tok.addEventListener(MouseEvent.CLICK, tapHandler);
                    }
                    break;
                default:
                    tok.addEventListener(TouchEvent.TOUCH_TAP, tapHandler);
                    tok.addEventListener(MouseEvent.CLICK, tapHandler);
                    break;
            }
        }
    }
}
