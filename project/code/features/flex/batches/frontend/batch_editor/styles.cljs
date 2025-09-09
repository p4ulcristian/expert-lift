(ns features.flex.batches.frontend.batch-editor.styles)

;; -----------------------------------------------------------------------------
;; ---- CSS Style Components ----

(defn- draggable-item-styles []
  "CSS styles for draggable items and selection states"
  ".draggable-item.selected {
     background-color: #e3f2fd !important;
     border-color: #1976d2 !important;
   }")

(defn- recipe-item-styles []
  "CSS styles for recipe items and their interactions"
  ".recipe-item {
     transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
   }
   
   .recipe-item:hover {
     transform: translateY(-2px);
     box-shadow: 0 4px 12px rgba(0,0,0,0.15);
   }
   
   .recipe-item:active {
     transform: translateY(0);
     transition: all 0.1s ease;
   }")

(defn- process-item-styles []
  "CSS styles for process items and their interactions"
  ".process-item {
     transition: all 0.2s ease;
   }
   
   .process-item:hover {
     box-shadow: 0 2px 8px rgba(0,0,0,0.1);
   }")

(defn- multi-drag-styles []
  "CSS styles for multi-drag functionality"
  ".multi-drag-ghost {
     opacity: 0.8;
     transform: rotate(5deg);
   }
   
   .multi-drag-counter {
     position: absolute;
     top: -8px;
     right: -8px;
     background: #ff4444;
     color: white;
     border-radius: 50%;
     width: 20px;
     height: 20px;
     display: flex;
     align-items: center;
     justify-content: center;
     font-size: 12px;
     font-weight: 600;
     z-index: 15;
     animation: bounceIn 0.3s ease;
   }
   @keyframes bounceIn {
     0% { transform: scale(0); }
     50% { transform: scale(1.2); }
     100% { transform: scale(1); }
   }")

(defn- timing-display-styles []
  "CSS styles for process timing displays"
  ".process-timing {
     display: flex;
     justify-content: flex-end;
     padding-top: 4px;
     border-top: 1px solid rgba(0,0,0,0.05);
     margin-top: 4px;
   }
   
   .timing-ready {
     color: #6366f1 !important;
   }
   
   .timing-active {
     color: #f59e0b !important;
     animation: pulse 2s infinite;
   }
   
   .timing-complete {
     color: #10b981 !important;
   }
   
   @keyframes pulse {
     0% { opacity: 1; }
     50% { opacity: 0.7; }
     100% { opacity: 1; }
   }")

(defn- container-styles []
  "CSS styles for main container elements"
  ".batch-editor-container {
     position: relative;
   }")

(defn batch-editor-styles [] 
  [:style (str
           (draggable-item-styles) "\n\n"
           (recipe-item-styles) "\n\n"
           (process-item-styles) "\n\n"
           (multi-drag-styles) "\n\n"
           (timing-display-styles) "\n\n"
           (container-styles))])