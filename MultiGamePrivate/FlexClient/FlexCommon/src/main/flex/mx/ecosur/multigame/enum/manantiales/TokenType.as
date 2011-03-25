package mx.ecosur.multigame.enum.manantiales
{
import mx.collections.ArrayCollection;
import mx.ecosur.multigame.enum.manantiales.Mode;

public class TokenType
    {
        public static const UNDEVELOPED:String = "UNDEVELOPED";
        public static const INTENSIVE:String = "INTENSIVE_PASTURE";
        public static const MODERATE:String = "MODERATE_PASTURE";
        public static const FOREST:String = "MANAGED_FOREST";
        public static const VIVERO:String = "VIVERO";
        public static const SILVOPASTORAL:String = "SILVOPASTORAL";

        public static function values (mode:String):ArrayCollection {
            var ret:ArrayCollection = new ArrayCollection();
            ret.addItem(UNDEVELOPED);
            ret.addItem(INTENSIVE);
            ret.addItem(MODERATE);
            ret.addItem(FOREST);
            if (mode == Mode.SILVOPASTORAL || mode == Mode.SILVO_PUZZLE) {
                ret.addItem(SILVOPASTORAL);
                if (mode == Mode.SILVOPASTORAL)
                    ret.addItem(VIVERO);
            }

            return ret;
        }

	}
}