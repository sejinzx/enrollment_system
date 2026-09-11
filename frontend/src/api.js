export function decodeUser(token) {
  try {
    const raw = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const data = JSON.parse(new TextDecoder().decode(Uint8Array.from(atob(raw), c => c.charCodeAt(0))));
    if (!data.exp || data.exp * 1000 <= Date.now()) return null;
    return { userId: data.userId, role: data.role?.replace(/^ROLE_/, '') };
  } catch { return null; }
}
export async function request(path, {token, body, ...options} = {}) {
  let response;
  try { response = await fetch(`/api${path}`, {...options, headers:{...(body ? {'Content-Type':'application/json'} : {}), ...(token ? {Authorization:`Bearer ${token}`} : {})}, ...(body ? {body:JSON.stringify(body)} : {})}); }
  catch { throw new Error('서버에 연결할 수 없습니다. 서버 실행 상태를 확인해 주세요.'); }
  const text = await response.text();
  let data; try { data = text ? JSON.parse(text) : {}; } catch { data = {}; }
  if (!response.ok) { const error = new Error(data.error || data.message || (response.status === 401 ? '로그인이 필요하거나 세션이 만료되었습니다.' : '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.')); error.status = response.status; throw error; }
  return data;
}
