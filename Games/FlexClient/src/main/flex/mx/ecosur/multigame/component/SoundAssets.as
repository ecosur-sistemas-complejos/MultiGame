/**
 * Contains sound assets that are shared across components.
 *
 * awaterma@ecosur.mx
 *
 *
 * Licensing information:
 *
 * Sound file downloaded from "freesound.org", provided by user "oniwe" and "simon.rue":
 *
 * http://www.freesound.org/samplesViewSingle.php?id=32864
 *
 * and
 *
 * http://www.freesound.org/samplesViewSingle.php?id=49944
 *
 * Converted from .wav format for embedding by awaterma.  Used under the CreativeCommons
 * Sample Plus license:
 *
 * http://creativecommons.org/licenses/sampling+/1.0/
 *
 * The sound has been sampled and reused as an integral part of the grid game implementations
 * in this work.
 */

package mx.ecosur.multigame.component {

    import flash.media.Sound;

    public class SoundAssets {
    
        [Embed(source="/assets/32864__oniwe__smallAudienceClapsMaleYeah.mp3")]
        private static var _goodSndCls:Class;
        private static var _goodSnd:Sound;

        [Embed(source="/assets/49944__simon.rue__misslyckad_bana_v1.mp3")]
        private static var _badSndCls:Class;
        private static var _badSnd:Sound;

        public static function get approval ():Sound {
            return new _goodSndCls() as Sound;
        }

        public static function get failure ():Sound {
            return new _badSndCls() as Sound;
        }
    }
}