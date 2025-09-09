const CDP = require('chrome-remote-interface');

async function streamConsole() {
  try {
    const client = await CDP();
    const {Runtime} = client;
    
    // Enable runtime events
    await Runtime.enable();
    
    console.log('🔗 Connected to Chrome DevTools Protocol');
    console.log('📡 Streaming console output from browser...\n');
    
    // Listen to console API calls (console.log, console.error, etc.)
    Runtime.consoleAPICalled((params) => {
      const timestamp = new Date().toISOString().split('T')[1].split('.')[0];
      const type = params.type.toUpperCase();
      const args = params.args.map(arg => {
        if (arg.type === 'string') return arg.value;
        if (arg.type === 'number') return arg.value;
        if (arg.type === 'boolean') return arg.value;
        if (arg.type === 'object') return arg.description || JSON.stringify(arg.preview);
        return arg.description || arg.value || '[object]';
      });
      
      const prefix = type === 'LOG' ? '📝' : type === 'ERROR' ? '❌' : type === 'WARN' ? '⚠️' : '🔍';
      console.log(`${prefix} [${timestamp}] ${type}: ${args.join(' ')}`);
    });
    
    // Listen to runtime exceptions (unhandled errors)
    Runtime.exceptionThrown((params) => {
      const timestamp = new Date().toISOString().split('T')[1].split('.')[0];
      const error = params.exceptionDetails;
      const exception = error.exception;
      
      console.log(`💥 [${timestamp}] EXCEPTION: ${error.text}`);
      console.log(`   📍 Location: ${error.url}:${error.lineNumber}:${error.columnNumber}`);
      
      // Show detailed exception description if available
      if (exception && exception.description) {
        console.log(`   📝 Details: ${exception.description}`);
      }
      
      // Show full stack trace if available
      if (error.stackTrace && error.stackTrace.callFrames) {
        console.log(`   📚 Stack Trace:`);
        error.stackTrace.callFrames.forEach((frame, index) => {
          const functionName = frame.functionName || '<anonymous>';
          const location = `${frame.url}:${frame.lineNumber}:${frame.columnNumber}`;
          console.log(`     ${index + 1}. ${functionName} (${location})`);
        });
      }
      
      console.log(''); // Empty line for readability
    });
    
    // Handle disconnection
    client.on('disconnect', () => {
      console.log('❌ Disconnected from Chrome DevTools Protocol');
      process.exit(1);
    });
    
  } catch (error) {
    console.error('Failed to connect to Chrome DevTools Protocol');
    console.error('Make sure Chrome is running with: --remote-debugging-port=9222');
    console.error('Error:', error.message);
    process.exit(1);
  }
}

// Handle graceful shutdown
process.on('SIGINT', () => {
  console.log('\n👋 Stopping console stream...');
  process.exit(0);
});

process.on('SIGTERM', () => {
  console.log('\n👋 Stopping console stream...');
  process.exit(0);
});

streamConsole();