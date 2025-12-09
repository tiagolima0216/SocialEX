// components/MessagesTab.tsx
import { useState } from 'react';
import { useMessages, type Message } from '../hooks/useMessages';
import { API } from '../api/api';

export default function MessagesTab() {
    const { conversations, setConversations } = useMessages();
    const [activeFriendId, setActiveFriendId] = useState<string | null>(null);
    const [newMessage, setNewMessage] = useState('');

    const activeConv = conversations.find(c => c.friendId === activeFriendId);

    const sendMessage = async () => {
        if (!activeFriendId || !newMessage.trim()) return;
        const payload = { content: newMessage };
        await API.post(`/api/v1/messages/${activeFriendId}`, payload);

        setNewMessage('');
        // message will arrive via websocket and update conversations
    };

    return (
        <div className="flex h-full">
            {/* Conversation List */}
            <div className="w-1/3 border-r overflow-y-auto">
                {conversations.map(c => (
                    <div
                        key={c.friendId}
                        className={`p-2 cursor-pointer ${c.friendId === activeFriendId ? 'bg-gray-200' : ''}`}
                        onClick={() => setActiveFriendId(c.friendId)}
                    >
                        <span className="font-medium">{c.friendUsername}</span>
                        {c.messages.some(m => !m.readFlag) && <span className="text-sm text-red-500 ml-2">•</span>}
                    </div>
                ))}
            </div>

            {/* Messages Pane */}
            <div className="flex-1 flex flex-col">
                <div className="flex-1 overflow-y-auto p-2">
                    {activeConv?.messages.map((m: Message) => (
                        <div
                            key={m.id}
                            className={`mb-2 ${m.senderId === activeFriendId ? 'text-left' : 'text-right'}`}
                        >
                            <div className="inline-block p-2 rounded-lg bg-gray-200">{m.content}</div>
                        </div>
                    ))}
                </div>
                {activeFriendId && (
                    <div className="flex p-2 border-t">
                        <input
                            className="flex-1 border rounded px-2 py-1"
                            value={newMessage}
                            onChange={e => setNewMessage(e.target.value)}
                            placeholder="Type a message..."
                        />
                        <button onClick={sendMessage} className="ml-2 bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded">
                            Send
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
