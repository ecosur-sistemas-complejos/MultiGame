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
import mx.ecosur.multigame.enum.GameState;
import mx.ecosur.multigame.enum.MoveStatus;
import mx.ecosur.multigame.manantiales.RoundCell;
import mx.ecosur.multigame.manantiales.entity.Ficha;
import mx.ecosur.multigame.manantiales.entity.ManantialesGame;
import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
import mx.ecosur.multigame.manantiales.enum.TokenType;
import mx.ecosur.multigame.manantiales.token.ForestToken;
import mx.ecosur.multigame.manantiales.token.IntensiveToken;
import mx.ecosur.multigame.manantiales.token.ManantialesToken;
import mx.ecosur.multigame.manantiales.token.ModerateToken;
import mx.ecosur.multigame.manantiales.token.SilvopastoralToken;
import mx.ecosur.multigame.manantiales.token.UndevelopedToken;
import mx.ecosur.multigame.manantiales.token.ViveroToken;

import mx.events.EffectEvent;
import mx.messaging.events.MessageEvent;
import mx.messaging.events.MessageFaultEvent;
import mx.messaging.messages.IMessage;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

import spark.effects.Animate;
import spark.effects.Move;
import spark.effects.animation.MotionPath;
import spark.effects.animation.SimpleMotionPath;
import spark.effects.easing.Bounce;

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
            trace("Fault: " + event.fault);
    
        }
    
        public function messageResultHandler(event:MessageEvent):void {
            var message:IMessage = event.message;
            var gameEvent:String = message.headers.GAME_EVENT;
            
            trace("Received message: " + message);
            trace("Headers: " + message.headers);
            trace("GameEvent: " + gameEvent);
        }
    
        public function messageFaultHandler(fault:MessageFaultEvent):void {
            trace("MessageFault: " + fault);
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

            if (_view.game.state = GameState.PLAY) {
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
            populateScores(_view.game);

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
                        tok.addEventListener(TouchEvent.TOUCH_TAP, tokenTapHandler);
                        tok.addEventListener(MouseEvent.CLICK, tokenTapHandler);
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
                }
            }
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

        public function populateScores(game:ManantialesGame):void {
            _view.scorebox.game = game;
           _view.scorebox.updatePlayers();
        }

        public function tokenTapHandler(event:Event):void {
            var dest:Point = new Point();
            var targ:UndevelopedToken = UndevelopedToken(event.target);
            var type:Object = _view.tokenTypes.getItemAt(_view.tokenType.selectedIndex);            
            var rc:RoundCell = RoundCell(_view.board.getBoardCell(targ.col, targ.row));
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
                animate(targ,  tok,  rc,  dest);

                var move:ManantialesMove = new ManantialesMove();
                move.status = MoveStatus.UNVERIFIED;
                move.mode = _view.game.mode;
                move.player = _view.player;
                move.currentCell = targ.ficha;
                move.destinationCell = tok.ficha;
                sendMove(move);
            }
        }

        private function animate(start:ManantialesToken, end:ManantialesToken,  roundCell:RoundCell,  point:Point):void {
            /* Bounce easing */
            var bounce:Bounce = new Bounce();

            /* Drop effect */
            
            /* Removing this as having issues with child not being appended */
            /*
            var drop:Move = new Move();
            drop.target = start;
            drop.xFrom = point.x;
            drop.yFrom = point.y;
            drop.xTo = point.x;
            drop.yTo = _view.board.height - start.y;
            drop.duration = 3000;            
            drop.easer = b1;
            drop.addEventListener(EffectEvent.EFFECT_END, removeTarget);
            */

            /* Set the roundcell token */
            roundCell.token = end;

            /* Resize token */
            var grw:Animate = new Animate();
            grw.easer = bounce;
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
            grw.play();
        }
    
        private function removeTarget (event:EffectEvent):void {
            var mover:Move = Move(event.target);
            var tok:ManantialesToken = ManantialesToken(mover.target);
            _view.board.removeChild(tok);
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
    
        private function sendMove(move:ManantialesMove):void {
            _view.status.showMessage("Processing move ...");
            _view.gameService.doMove(_view.game, move);
        }
    }
}
