package mx.ecosur.multigame.manantiales.token
{
    import flash.events.MouseEvent;
    
    import mx.events.DragEvent;	
	
	public class ViveroTokenStore extends ManantialesTokenStore
	{
		public function ViveroTokenStore()
		{
			super();
		}
		
        public override function addToken():void{
            var token:ViveroToken = new ViveroToken();
            token.buttonMode = false;
            token.addEventListener(MouseEvent.MOUSE_OVER, selectToken);
            token.addEventListener(MouseEvent.MOUSE_OUT, unselectToken);
            if (_startMoveHandler != null){
                    token.addEventListener(MouseEvent.MOUSE_DOWN, _startMoveHandler);
                }
            if (_endMoveHandler != null){
                token.addEventListener(DragEvent.DRAG_COMPLETE, _endMoveHandler);           
            }
            addChild(token);
            _nTokens ++;
        } 		
		
	}
}