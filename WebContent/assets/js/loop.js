/**
 * 2020 Sergio Soriano - sergiosoriano.com
 */
class Loop {

    constructor() {

    }

    start = function(listener, fps) {
        this.tgtTime = 1000.0 / fps;
        this.start = this.end = new Date().getTime();
        this.listener = listener;
        requestAnimationFrame(this.loop.bind(this));
    }

    stop = function() {
        this.listener = null;
    }

    loop = function() {
        if(this.listener) {
            let dt = (this.start - this.end) / 1000.0;
            this.end = this.start;
            this.start = new Date().getTime();

            this.listener(dt);
            
            let diff = this.tgtTime - (this.start - new Date().getTime());
            if(diff > 0) {
                let self = this;
                window.setTimeout(function() {
                    requestAnimationFrame(self.loop.bind(self));
                }, diff);
            } else {
                requestAnimationFrame(this.loop.bind(this));
            }
        }
    }

}