package mx.ecosur.multigame.component {
    import mx.controls.Image;
    import flash.display.Loader;
    import flash.display.Bitmap;
    import flash.events.Event;
    import mx.core.mx_internal;

    use namespace mx_internal;

    /**
     * SmoothImage
     *
     * Automatically turns smoothing on after image has loaded
     *
     * Taken from http://cookbooks.adobe.com/index.cfm?event=showdetails&postId=4001    
     * 
     * @author Ben Longoria
     */
    public class SmoothImage extends Image {

        private var _completed:Boolean = false;

        public function SmoothImage():void {
            super();
        }

        public function get complete():Boolean {
            return _completed;
        }

        public function set complete(done:Boolean):void {
            _completed = done;
        }

        /**
         * @private
         */
        override mx_internal function contentLoaderInfo_completeEventHandler(event:Event):void {
            var smoothLoader:Loader = event.target.loader as Loader;
            var smoothImage:Bitmap = smoothLoader.content as Bitmap;
            smoothImage.smoothing = true;
            _completed = true;
            super.contentLoaderInfo_completeEventHandler(event);
        }
    }
}