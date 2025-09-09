(ns features.customizer.panel.frontend.blocks.canvas.loader
  (:require
    ["@react-three/drei" :as r3d]))

(def loader-css
  [:style {:type "text/css"}
   "
    /* Loader vars */
    div.loader {
      --background-color: hsl(0, 0, 15);
      --loader-time: 1.4s;
      --item-size: 5vw;
      --item-max-size: 6vw;
      --item-color: green;
      --item-delay: 0s;
    }

    div.loader {
      display: grid;
      gap: 6px;
      grid-template-columns: repeat(1, 1fr);
      padding: 100px;
      background: #d1d2d5;
      transform-style: preserve-3d;
      transform: rotateX(65deg) rotate(290deg);
    }

    div.loader .item {
      overflow: unset;
      position: relative;
      min-width: 30px;
      min-height: 30px;
      width: var(--item-size);
      height: var(--item-size);
      background: var(--irb-clr);
      transform-style: preserve-3d;
      animation: anim-move var(--loader-time) ease-in-out infinite var(--item-delay);
    }

    div.loader .item .lighter,
    div.loader .item .darker {
      position: absolute;
      min-width: 30px;
      min-height: 30px;
      width: var(--item-size);
      height: calc(var(--item-size) / 2);
      animation: anim-height var(--loader-time) ease-in-out infinite var(--item-delay);
    }

    div.loader .item .lighter {
      background: hsl(50deg 85.09% 50.93%);
      transform-origin: 50% 0%;
      transform: translateY(var(--item-size)) rotateX(-90deg);
    }

    div.loader .item .darker {
      display: flex;
      align-items: center;
      justify-content: center;
      background: hsl(50deg 74.36% 40.24%);
      transform-origin: 0% 0%;
      transform: rotate(90deg) rotateX(-90deg);
      font-size: 2.5vw;
    }

    @keyframes anim-move {
      0%, 100% {
        transform: translateZ(0);
      }
      40%, 60% {
        transform: translateZ(var(--item-max-size));
      }
    }

    @keyframes anim-height {
      0%, 100% {
        height: calc(var(--item-size) / 2);
      }
      40%, 60% {
        height: var(--item-max-size);
      }
    }
      
      @keyframes pulse {
      0%, 100% {
        opacity: 0.4;
      }
      40%, 60% {
        opacity: 1;
      }
    }
"])
(defn loader-letter [index _char]
  [:div {:key   (str _char index)
         :class (str "item item-" index)
         :style {"--item-delay" (str (* index 0.1) "s")}}
    [:div {:class "lighter"}]
    [:div {:class "darker"}
      _char]])            

(defn view []
  [:> r3d/Html {"center" true}
    loader-css
    [:div {:class "loader"}
      (doall 
        (map-indexed loader-letter ["L" "O" "A" "D" "I" "N" "G"]))]])