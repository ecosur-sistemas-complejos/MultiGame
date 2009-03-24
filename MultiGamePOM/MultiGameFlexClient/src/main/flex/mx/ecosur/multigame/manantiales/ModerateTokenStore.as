package mx.ecosur.multigame.manantiales
{
    import flash.events.MouseEvent;
    
    import mx.events.DragEvent;	
	
	
	public class ModerateTokenStore extends ManantialesTokenStore
	{
		public function ModerateTokenStore()
		{
			super();
		}
		
        public override function addToken():void{
            var token:ModerateToken = new ModerateToken();
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