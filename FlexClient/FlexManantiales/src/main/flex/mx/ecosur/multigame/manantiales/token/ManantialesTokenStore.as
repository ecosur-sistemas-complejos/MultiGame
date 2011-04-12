package mx.ecosur.multigame.manantiales.token
{
    import flash.display.Shape;
    import flash.events.MouseEvent;

    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.component.TokenStore;
    import mx.ecosur.multigame.manantiales.ManantialesGameController;
    import mx.ecosur.multigame.entity.manantiales.ManantialesMove;
    import mx.events.DragEvent;
    import mx.managers.DragManager;

    public class ManantialesTokenStore extends TokenStore
    {
        protected static const INITIAL_N_TOKENS:int = 6;
        protected var _tokenType:String;
        protected var _controller:ManantialesGameController;
        private var _initialized:Boolean;

        public function ManantialesTokenStore() {
            super();
            addEventListener(DragEvent.DRAG_ENTER,dragEnter);
            addEventListener(DragEvent.DRAG_DROP, dragDrop);
            _initialized = false;
        }

        override protected function createChildren():void{

            // Create background
            _bg = new Shape();
            addChild(_bg);

            // Create initial tokens
            _nTokens = 0;
            fill();
        }

        override protected function selectToken(event:MouseEvent):void {
            if (event.target is Token) {
                var target:ManantialesToken = ManantialesToken(event.target);
                target.selected = true;
                target.placed = false;
            }
        }

        public function init(tokens:ArrayCollection):void {
            if (!_initialized) {
                for (var i:int = 0; i < tokens.length; i++) {
                    var token:ManantialesToken = ManantialesToken (tokens.getItemAt(i));
                    if (token && token.cell && token.cell.color == _controller._currentPlayer.color && 
                            token.type == _tokenType) {
                        removeToken();
                    } 
                }

                _initialized = true;
            }
        }

        public function fill():void {
            for (var i:int = _nTokens; i < INITIAL_N_TOKENS; i++) {
                addToken();
            }
        }

        public function get controller():ManantialesGameController {
            return _controller;
        }

        public function set controller(controller:ManantialesGameController):void {
            _controller = controller;
        }
        
        public function get tokenType():String {
            return _tokenType;
        }

        protected function dragEnter(evt:DragEvent):void{

            if (evt.dragSource.hasFormat("token")){
                var token:ManantialesToken = ManantialesToken(evt.dragSource.dataForFormat("token"));

                if(token.placed && token.type == _tokenType && token.cell.color == _currentPlayer.color){
                    DragManager.acceptDragDrop(this);
                }
            }
        }

        protected function dragDrop(evt:DragEvent):void{
            if (_nTokens < INITIAL_N_TOKENS && evt.dragSource.hasFormat("token")) {
                var token:ManantialesToken = ManantialesToken(evt.dragSource.dataForFormat("token"));
                addToken();

                 /* Send "remove" move to controller */
                var move:ManantialesMove = new ManantialesMove ();
                move.currentCell = token.ficha;
                move.destinationCell = null;
                move.player = _controller._currentPlayer;
                move.mode = _controller._game.mode;
                _controller.sendMove(move);
            }

        }
    }

}