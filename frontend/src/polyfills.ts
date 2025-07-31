/***************************************************************************************************
 * BROWSER POLYFILLS
 */
(window as any).global = window;
(window as any).process = {
  env: { DEBUG: undefined },
  version: ''
};

if (!crypto.randomUUID) {
  crypto.randomUUID = () => {
    return (
      'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
      }) as `${string}-${string}-${string}-${string}-${string}`
    );
  };
}


/***************************************************************************************************
 * APPLICATION IMPORTS
 */