package mx.ecosur.multigame.manantiales.token
{
    import flash.display.Shape;
    import flash.events.MouseEvent;

    import mx.collections.ArrayCollection;
    import mx.core.InteractionMode;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.component.TokenStore;
    import mx.ecosur.multigame.event.DragMoveEvent;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
    import mx.events.DragEvent;
    import mx.managers.DragManager;

    [Event (name="DragAndDrop", type="mx.ecosur.multigame.event.MoveEvent")]
    public class ManantialesTokenStore extends TokenStore
    {
        protected static const INITIAL_N_TOKENS:int = 6;
        protected var _tokenType:String;
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
                target.setStyle ('interactionMode', InteractionMode.MOUSE);
                target.selected = true;
                target.placed = false;
            }
        }

        public function init(player:ManantialesPlayer):void {

            if (!_initialized) {
                var removals:int = 0;
                switch (tokenType) {
                    case TokenType.FOREST:
                        removals = player.forested;
                        break;
                    case TokenType.MODERATE:
                        removals = player.moderate;
                        break;
                    case TokenType.INTENSIVE:
                        removals = player.intensive;
                        break;
                    case TokenType.VIVERO:
                        removals = player.vivero;
                        break;
                    case TokenType.SILVOPASTORAL:
                        removals = player.silvo;
                        break;
                }

                for (var i:int = 0; i < removals; i++) {
                    removeToken();
                }

                _initialized = true;
            }
        }

        public function fill():void {
            for (var i:int = _nTokens; i < INITIAL_N_TOKENS; i++) {
                addToken();
            }
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

                var event:DragMoveEvent = new DragMoveEvent("DragAndDrop", move);
                dispatchEvent(event);

            }

        }
    }

}
