export const ITERATIONS = 600000;
export const PLAIN_LIMIT = 20000;
export const CIPHER_LIMIT = 120000;
const encoder = new TextEncoder();
export function encode64(bytes) {
  let binary = ''; for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary).replace(/\+/g,'-').replace(/\//g,'_').replace(/=+$/,'');
}
function decode64(value) {
  if (!/^[A-Za-z0-9_-]+$/.test(value)) throw new Error('Invalid encoding.');
  const bytes = Uint8Array.from(atob(value.replace(/-/g,'+').replace(/_/g,'/')),c=>c.charCodeAt(0));
  if (encode64(bytes) !== value) throw new Error('Noncanonical encoding.');
  return bytes;
}
function validate(value,key,decrypt=false) {
  if (!value.trim() || value.length > (decrypt?CIPHER_LIMIT:PLAIN_LIMIT)) throw new Error('Text is empty or exceeds the documented size limit.');
  if (!key.trim() || key.length > 1024) throw new Error('Use a passphrase between 1 and 1,024 characters.');
  if (!globalThis.crypto?.subtle) throw new Error('Local cryptography needs a supported browser on HTTPS. No data was sent.');
}
async function derive(password,salt,iterations,name) {
  const material = await crypto.subtle.importKey('raw',encoder.encode(password),'PBKDF2',false,['deriveKey']);
  return crypto.subtle.deriveKey({name:'PBKDF2',salt,iterations,hash:'SHA-256'},material,{name,length:128},false,['encrypt','decrypt']);
}
export async function encryptLocal(text,password) {
  validate(text,password);
  const salt=crypto.getRandomValues(new Uint8Array(16)),nonce=crypto.getRandomValues(new Uint8Array(12));
  const header=`SC1.${ITERATIONS}.${encode64(salt)}.${encode64(nonce)}`;
  const key=await derive(password,salt,ITERATIONS,'AES-GCM');
  const bytes=await crypto.subtle.encrypt({name:'AES-GCM',iv:nonce,additionalData:encoder.encode(header),tagLength:128},key,encoder.encode(text));
  return header+'.'+encode64(new Uint8Array(bytes));
}
export function inspectEnvelope(text) {
  const parts=text.trim().split('.');
  if(parts.length!==5 || parts[0]!=='SC1' || parts[1]!==String(ITERATIONS)) throw new Error('Unsupported format. For old outputs, select legacy AES-CBC explicitly.');
  const salt=decode64(parts[2]),nonce=decode64(parts[3]),ciphertext=decode64(parts[4]);
  if(salt.length!==16 || nonce.length!==12 || ciphertext.length<16 || ciphertext.length>80016) throw new Error('Invalid encrypted message.');
  return {salt,nonce,ciphertext,header:parts.slice(0,4).join('.')};
}
export async function decryptLocal(text,password) {
  validate(text,password,true);
  try {
    const {salt,nonce,ciphertext,header}=inspectEnvelope(text);
    const key=await derive(password,salt,ITERATIONS,'AES-GCM');
    const bytes=await crypto.subtle.decrypt({name:'AES-GCM',iv:nonce,additionalData:encoder.encode(header),tagLength:128},key,ciphertext);
    const result=new TextDecoder('utf-8',{fatal:true}).decode(bytes);
    validate(result,password); return result;
  } catch { throw new Error('Cannot decrypt: wrong passphrase, modified message, or unsupported format. Old AES outputs require legacy mode.'); }
}
export async function decryptLegacyLocal(text,password) {
  validate(text,password,true);
  try {
    const bytes=Uint8Array.from(atob(text.trim()),c=>c.charCodeAt(0));
    if(bytes.length<32 || bytes.length%16) throw new Error();
    const key=await derive(password,encoder.encode('SecureCrypt$Salt'),65536,'AES-CBC');
    const plain=await crypto.subtle.decrypt({name:'AES-CBC',iv:bytes.slice(0,16)},key,bytes.slice(16));
    const result=new TextDecoder('utf-8',{fatal:true}).decode(plain);validate(result,password);return result;
  } catch { throw new Error('Legacy decryption failed. Old CBC messages cannot prove integrity.'); }
}
