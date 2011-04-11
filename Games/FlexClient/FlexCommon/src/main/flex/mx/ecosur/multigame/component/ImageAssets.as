package mx.ecosur.multigame.component {
import mx.controls.Image;

public class ImageAssets {

        [Embed(source="/assets/dirt.png")]
        public static var dirtCls:Class;

        [Embed(source="/assets/forest.png")]
        public static var forestCls:Class;

        [Embed(source="/assets/farm.svg")]
        [Bindable]
        public static var farmCls:Class;
    
    }
}